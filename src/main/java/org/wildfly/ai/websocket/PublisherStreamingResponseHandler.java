/*
 * Copyright 2025 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.ai.websocket;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.output.Response;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class PublisherStreamingResponseHandler implements StreamingResponseHandler<AiMessage>, Publisher<String> {

    private Subscriber subscriber;
    private boolean running = true;

    public PublisherStreamingResponseHandler(){
    }

    @Override
    public void onNext(String token) {
        subscriber.onNext(token);
    }

    public boolean isRunning() {
        return running;
    }

    void stop() {
        synchronized (this) {
            this.running = false;
        }
    }

    @Override
    public void onComplete(Response<AiMessage> response) {
        subscriber.onComplete();
    }

    @Override
    public void onError(Throwable error) {
        subscriber.onError(error);
    }

    @Override
    public void subscribe(Subscriber<? super String> subscriber) {
        this.subscriber = subscriber;
        this.subscriber.onSubscribe(new Subscription() {
            @Override
            public void request(long l) {
                 if (l != 1) {
                    throw new IllegalArgumentException("You can only request one entry. Instead you requested " + l);
                }
            }

            @Override
            public void cancel() {
                stop();
                PublisherStreamingResponseHandler.this.subscriber = null;
            }
        });
    }
}
