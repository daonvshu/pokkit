#pragma once

#include "utils/datakey.h"

using namespace QDataUtil;

template<typename T>
struct DataDumpProtocol : DataDumpInterface {
    //json数据解码
    static T fromJson(const QJsonDocument& doc) {
        T data;
        auto obj = doc.object();
        data.DataDumpInterface::fromJson(obj);
        return data;
    }

    //json数据编码
    QJsonDocument toJson() const {
        return QJsonDocument(this->dumpToJson());
    }
};