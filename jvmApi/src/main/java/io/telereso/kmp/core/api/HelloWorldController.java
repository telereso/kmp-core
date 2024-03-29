/*
 * MIT License
 *
 * Copyright (c) 2023 Telereso
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.telereso.kmp.core.api;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import io.telereso.kmp.core.Task;
import io.telereso.kmp.core.TaskConfig;
import io.telereso.kmp.core.Tasks;
import io.telereso.kmp.core.TasksExamples;


@Controller
// Main class
public class HelloWorldController {

    @RequestMapping("")
    @ResponseBody
    public String hi() {

        try {
//            TasksExamples.hi().get();
//            Tasks.create((Callable<String>) () -> "").get();
            TasksExamples.testRetry3(TaskConfig
                    .builder()
                    .retry(5)
                    .backOffDelay(1000)
                    .build()).get();
            return Tasks.future(TasksExamples.exception()).get();
        } catch (Exception e ) {
            return new RuntimeException(e).getMessage();
        }
    }
}