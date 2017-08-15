package com.incarcloud.rooster.datapack;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DataParserGBT32960 implements IDataParser {

    @Override
    public List<DataPack> extract(ByteBuf buffer) {
        /*  0,1: start with '##'(0x23 0x23)
              2: 命令
              3: 应答标志
           4~20: VIN
             21: 加密方式
          22,23: 数据单元长度网络序
            ...: 数据单元
         最后1个: BCC异或校验(不含0,1和最后1个字节)

         + 最少25字节
         + 以'##'(0x23 0x23)标志位开始
         + BCC校验
        */
        final int frameLen = 25;
        List<DataPack> listPacks = new ArrayList<>();

        // 1. 找到'##'. 如果没找到,整个丢弃掉
        // 2. 获取包总长度
        // 3. 找到BCC位置. 如果BCC位置还没传过来,等更多数据
        // 4. 验证BCC. 如果验证失败,回到1

        // 包最小也得有25字节,没有25字节就不需要再处理了
        while (buffer.readableBytes() >= frameLen) {

            // 如果找到'##',那么'##'之前的丢弃掉,如果没找到,整个丢弃掉
            boolean bFound = false;
            for (int i = buffer.readerIndex(); i < buffer.writerIndex() - 1; i++) {
                if (buffer.getByte(i) == (byte) 0x23 && buffer.getByte(i + 1) == (byte) 0x23) {
                    bFound = true;
                    buffer.skipBytes(i);
                    break;
                }
            }
            if (!bFound) buffer.skipBytes(buffer.readableBytes() - 1);

            // 获取BCC字节
            if (bFound && buffer.readableBytes() >= frameLen) {
                int dataLen = buffer.getUnsignedShort(22);
                if (buffer.readableBytes() < dataLen + frameLen) break;

                // 验证BCC异或校验码
                byte bcc = 0x00;
                for (int j = 2; j < dataLen + frameLen - 1; j++) bcc ^= buffer.getByte(j);
                if (bcc == buffer.getByte(dataLen + frameLen - 1)) {
                    // okay!
                    DataPack pack = new DataPack("china", "gbt32960", "1.0.0");
                    ByteBuf buf = buffer.slice(0, dataLen + frameLen);
                    pack.setBuf(buf);
                    listPacks.add(pack);
                    buffer.skipBytes(dataLen + frameLen);
                } else {
                    // 只能跳过1个字节,因为有可能出现这种3个连续的#,而从第2个#开始算起,恰好是一个正常的数据包的情况 ###.....
                    buffer.skipBytes(1);
                }
            }
        }

        return listPacks;
    }

    @Override
    public ByteBuf createResponse(DataPack requestPack, ERespReason reason) {
        return null;
    }

    @Override
    public void destroyResponse(ByteBuf responseBuf) {

    }


    @Override
    public Map<String, Object> getMetaData(ByteBuf buffer) {
        return null;
    }

    @Override
    public List<DataPackTarget> extractBody(DataPack dataPack) {
        throw new UnsupportedOperationException();
    }
}
