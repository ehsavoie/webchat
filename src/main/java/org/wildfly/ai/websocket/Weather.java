/*
 * Copyright 2024 JBoss by Red Hat.
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

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import java.util.List;
import java.util.Random;

public class Weather {

    private static final List<String> WEATHER_CONDITIONS = List.of("Clear", "Partly Cloudy", "Cloudy", "Light Rain", "Heavy Rain", "Thunderstorm", "Snow", "Fog");
    private final Random random = new Random();

    @Tool("Get the current weather for a given location")
    String currentWeather(@P("The location for which the current weather should be returned")String location) {
        String condition = WEATHER_CONDITIONS.get(random.nextInt(0, WEATHER_CONDITIONS.size()));
        int temperature = random.nextInt(-10, 40);
        int humidity = random.nextInt(30, 90);
        int wind_speed = random.nextInt(0, 30);
        String result =  "Current weather in " + location + ":\n"
                + "- Condition: " + condition + "\n"
                + "- Temperature: " + temperature + "°C\n"
                + "- Humidity: " + humidity + "%\n"
                + "- Wind Speed: " + wind_speed + " km/h";
        System.out.println("Response is " + result);
        return result;
    }
}
