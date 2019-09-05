/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.journalkeeper.rpc.client;

import io.journalkeeper.exceptions.ServerBusyException;
import io.journalkeeper.rpc.LeaderResponse;
import io.journalkeeper.rpc.StatusCode;

/**
 * @author LiYue
 * Date: 2019-03-14
 */
public class UpdateClusterStateResponse extends LeaderResponse {
    public UpdateClusterStateResponse() {
        super();
        this.result = null;
    }
    public UpdateClusterStateResponse(byte [] result) {
        super();
        this.result = result;
    }
    public UpdateClusterStateResponse(Throwable exception) {
        super(exception);
        this.result = null;

    }
    private final byte [] result;


    /**
     * 序列化后的执行结果。
     * @return 序列化后的执行结果。
     */
    public byte [] getResult() {
        return result;
    }

    @Override
    public void setException(Throwable throwable) {
        try {
            throw throwable;
        } catch (ServerBusyException e) {
            setStatusCode(StatusCode.SERVER_BUSY);
        } catch (Throwable t) {
            super.setException(throwable);
        }
    }

}