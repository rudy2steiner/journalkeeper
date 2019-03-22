package com.jd.journalkeeper.persistence;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

/**
 * 日志持久化接口
 * @author liyue25
 * Date: 2019-03-14
 */
public interface JournalPersistence extends Closeable {
    /**
     * 最小位置，初始化为0
     */
    long min();

    /**
     * 最大位置， 初始化为0
     */
    long max();

    /**
     * 当前刷盘位置
     */
    default long flushed() { return max();}

    /**
     * 执行一次刷盘操作
     * @return 刷盘位置
     */
    default CompletableFuture<Long> flush() throws IOException {
        CompletableFuture<Long> completableFuture
                = new CompletableFuture<>();
        completableFuture.complete(flushed());
        return completableFuture;
    }
    /**
     * 截断最新的日志
     * @param givenMax 新的最大位置，不能大于当前的最大位置
     */
    CompletableFuture<Void> truncate(long givenMax);

    /**
     * 删除旧日志。考虑到大多数基于文件的实现做到精确按位置删除代价较大，
     * 不要求精确删除到给定位置。但不能删除给定位置之后的数据。
     * @param givenMin 给定删除位置，这个位置之前都可以删除。
     * @return 删除后当前最小位置。
     */
    CompletableFuture<Long> shrink(long givenMin);

    /**
     * 追加写入
     * @param byteBuffers 待写入的内容
     * @return 写入后新的位置
     */
    Long append(ByteBuffer... byteBuffers);

    /**
     * 读取数据
     * @param position 起始位置
     * @param length 读取长度
     * @return 存放数据的ByteBuffer
     */
    ByteBuffer read(long position, int length);

    /**
     * 从指定Path恢复Journal，如果没有则创建一个空的。
     * @param path journal存放路径
     * @param properties 属性
     */
    void recover(Path path, Properties properties);

}