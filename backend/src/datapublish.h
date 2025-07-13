#pragma once

#include <protocolcodecengine.h>

using namespace protocol_codec;
class DataPublishInterface {
public:
    virtual void publish(const std::function<QByteArray(ProtocolCodecEngine&)>&) = 0;

    ~DataPublishInterface() = default;
};