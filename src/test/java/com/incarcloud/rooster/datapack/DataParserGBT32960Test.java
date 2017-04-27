package com.incarcloud.rooster.datapack;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DataParserGBT32960Test {
    private static Logger s_logger = LoggerFactory.getLogger(DataParserGBT32960Test.class);

    @Test
    public void GBT32960DataSimpleTest(){
        byte[] data = {0x23, 0x23, 0x01, (byte)0xfe,
                0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37,
                0x01, 0x00, 36, // enc+len
                17, 2, 14, 17, 5, 19, // TIMESTAMP
                0x00, 0x07, // SN
                0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x30, // ICCID
                0x03, 0x02,
                0x31, 0x32, 0x31, 0x32, 0x31, 0x32,
                (byte)0xf4 };
        ByteBuf buffer = Unpooled.wrappedBuffer(data);
        IDataParser parser = new DataParserGBT32960();
        List<DataPack> listPacks = parser.extract(buffer);

        Assert.assertEquals(1, listPacks.size());

        for(DataPack pack : listPacks){
            pack.freeBuf();
        }

        buffer.release();
        Assert.assertEquals(0, buffer.refCnt());
    }
}
