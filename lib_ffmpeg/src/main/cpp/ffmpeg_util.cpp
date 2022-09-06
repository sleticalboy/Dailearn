//
// Created by binlee on 2022/9/5.
//

#include "ffmpeg_util.h"

namespace ffmpeg {
namespace util {

const int kBufferSize = 4096;

const char *AVFormatContextToString(AVFormatContext *ctx) {
  if (ctx == nullptr) {
    return nullptr;
  }

  char *buffer = new char[kBufferSize];
  int offset = 0;
  if (ctx->av_class != nullptr) {
    offset += sprintf(buffer + offset, "av_class: %s, ", ctx->av_class->class_name);
  }
  offset += sprintf(buffer + offset, "iformat: {%s}, ", AVInputFormatToString(ctx->iformat));
  offset += sprintf(buffer + offset, "oformat: {%s}, ", AVOutputFormatToString(ctx->oformat));
  offset += sprintf(buffer + offset, "ctx_flags: %d, ", ctx->ctx_flags);
  offset += sprintf(buffer + offset, "nb_streams: %d, ", ctx->nb_streams);
  offset += sprintf(buffer + offset, "url: %s, ", ctx->url);
  offset += sprintf(buffer + offset, "start_time: %ld, ", ctx->start_time);
  offset += sprintf(buffer + offset, "duration: %ld, ", ctx->duration);
  offset += sprintf(buffer + offset, "bit_rate: %ld, ", ctx->bit_rate);
  offset += sprintf(buffer + offset, "packet_size: %u, ", ctx->packet_size);
  offset += sprintf(buffer + offset, "max_delay: %d, ", ctx->max_delay);
  offset += sprintf(buffer + offset, "flags: 0x%x, ", ctx->flags);
  offset += sprintf(buffer + offset, "probesize: %ld, ", ctx->probesize);
  offset += sprintf(buffer + offset, "codec_whitelist: %s, ", ctx->codec_whitelist);
  offset += sprintf(buffer + offset, "format_whitelist: %s, ", ctx->format_whitelist);
  /*offset += */sprintf(buffer + offset, "protocol_whitelist: %s, ", ctx->protocol_whitelist);
  return buffer;
}

const char *AVInputFormatToString(const struct AVInputFormat *format) {
  if (format == nullptr) {
    return nullptr;
  }

  char *buffer = new char[kBufferSize];
  int offset = sprintf(buffer, "name: %s, ", format->name);
  offset += sprintf(buffer + offset, "long_name: %s, ", format->long_name);
  offset += sprintf(buffer + offset, "flags: %d, ", format->flags);
  offset += sprintf(buffer + offset, "extensions: %s, ", format->extensions);
  offset += sprintf(buffer + offset, "mime_type: %s, ", format->mime_type);
  offset += sprintf(buffer + offset, "raw_codec_id: %d, ", format->raw_codec_id);
  offset += sprintf(buffer + offset, "priv_data_size: %d, ", format->priv_data_size);
  /*offset += */sprintf(buffer + offset, "flags_internal: %d, ", format->flags_internal);
  return buffer;
}

const char *AVOutputFormatToString(const struct AVOutputFormat *format) {
  if (format == nullptr) {
    return nullptr;
  }

  char *buffer = new char[kBufferSize];
  int offset = sprintf(buffer, "name: %s, ", format->name);
  offset += sprintf(buffer + offset, "long_name: %s, ", format->long_name);
  offset += sprintf(buffer + offset, "flags: %d, ", format->flags);
  offset += sprintf(buffer + offset, "extensions: %s, ", format->extensions);
  offset += sprintf(buffer + offset, "mime_type: %s, ", format->mime_type);
  offset += sprintf(buffer + offset, "priv_data_size: %d, ", format->priv_data_size);
  /*offset += */sprintf(buffer + offset, "flags_internal: %d, ", format->flags_internal);
  return buffer;
}

} // namespace util
} // namespace ffmpeg