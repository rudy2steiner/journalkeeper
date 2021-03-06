/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.journalkeeper.rpc.remoting.transport.command;


import java.net.URI;

/**
 * @author hexiaofeng
 */
public interface Header {

    boolean isOneWay();

    void setOneWay(boolean isOneWay);

    /**
     * 状态
     *
     * @return 状态
     */
    int getStatus();

    /**
     * 状态
     *
     * @param status 状态
     */
    void setStatus(int status);

    /**
     * error
     *
     * @return 错误信息
     */
    String getError();

    /**
     * error
     *
     * @param msg 错误信息
     */
    void setError(String msg);

    /**
     * 请求ID
     *
     * @return 请求ID
     */
    int getRequestId();

    /**
     * 请求ID
     *
     * @param requestId 请求ID
     */
    void setRequestId(int requestId);

    /**
     * 获取数据包方向
     *
     * @return 数据包方向
     */
    Direction getDirection();

    /**
     * 设置数据包方向
     *
     * @param direction 方向
     */
    void setDirection(Direction direction);

    /**
     * 版本号
     *
     * @return 版本号
     */
    int getVersion();

    /**
     * 设置版本号
     *
     * @param version 版本号
     */
    void setVersion(int version);

    /**
     * 类型
     *
     * @return 类型
     */
    int getType();

    /**
     * 设置类型
     *
     * @param type 类型
     */
    void setType(int type);

    /**
     * 获取目标地址
     * @return 目标地址
     */
    URI getDestination();

    /**
     * 设置目标地址
     * @param uri 目标地址
     */
    void setDestination(URI uri);
}
