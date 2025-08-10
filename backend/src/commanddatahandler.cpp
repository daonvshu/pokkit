#include "commanddatahandler.h"

#include "utils/globalenv.h"

#include "providers/downloadserviceprovider.h"

#include <qtimer.h>
#include <qfile.h>
#include <qfileinfo.h>
#include <qprocess.h>
#include <qdir.h>
#include <QtConcurrent/QtConcurrent>

#include <ShlObj.h>

using namespace protocol_codec;

struct IdentifyAuthRequest : DataDumpProtocol<IdentifyAuthRequest> {
    enum {
        Type = 0
    };

    DATA_KEY(QString, role);

    QList<DataReadInterface *> prop() override {
        return { &role };
    }
};

struct ProxyInfoSync : DataDumpProtocol<ProxyInfoSync> {
    enum {
        Type = 1
    };

    DATA_KEY(QString, proxyAddress);
    DATA_KEY(int, proxyPort);

    QList<DataReadInterface *> prop() override {
        return { &proxyAddress, &proxyPort };
    }
};

CommandDataHandler::CommandDataHandler(IdentifyAuthConfirmedCallback* callback, QObject *parent)
    : QObject(parent)
    , callback(callback)
{
    codecEngine.frameDeclare("H(FAFE)S4CV(CRC16)E(FE)");
    codecEngine.setVerifyFlags("SC");

    codecEngine.registerType<JsonCodec<IdentifyAuthRequest>>(this, &CommandDataHandler::onIdentifyAuthRequest);
    codecEngine.registerType<JsonCodec<ProxyInfoSync>>(this, &CommandDataHandler::onProxyInfoSync);

    downloadServiceProvider = new DownloadServiceProvider(this, this);
    //request
    codecEngine.registerType<JsonCodec<TorrentContentFetchRequest>>(downloadServiceProvider, &DownloadServiceProvider::getTorrentContent);
    //codecEngine.registerType<JsonCodec<TorrentContentFetchRequest>>(this, &CommandDataHandler::sendBufferTest);
    codecEngine.registerType<TorrentContentFetchCancelRequest>(downloadServiceProvider, &DownloadServiceProvider::getTorrentContentCancel);
    codecEngine.registerType<JsonCodec<RequestOpenDir>>(this, &CommandDataHandler::onRequestOpenDir);
    codecEngine.registerType<JsonCodec<TorrentDownloadRequest>>(downloadServiceProvider, &DownloadServiceProvider::beginDownload);
    codecEngine.registerType<JsonCodec<TorrentPauseOrResumeRequest>>(downloadServiceProvider, &DownloadServiceProvider::onTorrentPauseOrResumeRequest);
    //feedback
    codecEngine.registerType<TorrentContentFetchProgressUpdate, JsonCodec>();
    codecEngine.registerType<TorrentContentFetchResult, JsonCodec>();
    codecEngine.registerType<TorrentStatusList, JsonCodec>();
}

void CommandDataHandler::addBuffer(const QByteArray &data) {
    codecEngine.appendBuffer(data);
}

void CommandDataHandler::onIdentifyAuthRequest(const IdentifyAuthRequest &request) {
    if (request.role() == "Pokkit/ReadChannel") {
        callback->onReadChannelReady();
    }
}

void CommandDataHandler::onProxyInfoSync(const ProxyInfoSync &request) {
    GlobalEnv::setProxy(request.proxyAddress(), request.proxyPort());
}

void CommandDataHandler::publish(const std::function<QByteArray(protocol_codec::ProtocolCodecEngine &)> &getData) {
    auto buffer = getData(codecEngine);
    emit dataFeedback(buffer);
    //QFile file("test.bin");
    //file.open(QIODevice::WriteOnly | QIODevice::Append);
    //file.write(buffer);
    //file.close();
}

void CommandDataHandler::sendBufferTest(const TorrentContentFetchRequest &request) {
    QFile file("test.bin");
    file.open(QIODevice::ReadOnly);
    auto data = file.readAll();
    emit dataFeedback(data);
    file.close();
}

void CommandDataHandler::onRequestOpenDir(const RequestOpenDir &request) {
    auto paths = request.paths();
    if (paths.isEmpty()) {
        return;
    }
    qInfo() << "open and select files:" << paths;

    if (!SUCCEEDED(CoInitializeEx(NULL, COINIT_APARTMENTTHREADED | COINIT_DISABLE_OLE1DDE))) {
        return;
    }

    // 获取文件夹路径
    QFileInfo firstFile(paths.first());
    QString folderPath = firstFile.absolutePath().replace("/", "\\");

    PIDLIST_ABSOLUTE folderPIDL = nullptr;
    HRESULT hr = SHParseDisplayName((LPCWSTR)folderPath.utf16(), nullptr, &folderPIDL, 0, nullptr);
    if (FAILED(hr) || !folderPIDL) {
        CoUninitialize();
        return;
    }

    // 创建文件PIDL数组
    QList<PCUITEMID_CHILD> itemPIDLs;
    for (const QString &filePath : paths) {
        PIDLIST_ABSOLUTE filePIDL = nullptr;
        hr = SHParseDisplayName((LPCWSTR)filePath.utf16(), nullptr, &filePIDL, 0, nullptr);
        if (SUCCEEDED(hr) && filePIDL) {
            itemPIDLs.append(ILFindLastID(filePIDL)); // 获取相对于文件夹的子ID
        }
    }

    if (itemPIDLs.isEmpty()) {
        CoTaskMemFree(folderPIDL);
        CoUninitialize();
        return;
    }

    // 打开资源管理器并选中指定文件
    hr = SHOpenFolderAndSelectItems(folderPIDL, static_cast<UINT>(itemPIDLs.size()), itemPIDLs.data(), 0);

    // 清理
    CoTaskMemFree(folderPIDL);

    CoUninitialize();
}

void CommandDataHandler::listenTorrentUpdateStart() {
    downloadServiceProvider->publishTorrentStatus();
}
