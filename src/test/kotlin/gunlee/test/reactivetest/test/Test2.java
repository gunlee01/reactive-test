/*
 *  Copyright 2015 the original author or authors.
 *  @https://github.com/scouter-project/scouter
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package gunlee.test.reactivetest.test;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2020/08/07
 */
public class Test2 {

    public void test() {
        int a = 1;
        long b = 2;
        String c = "3";

        Object[] objs = {a, b, c};
        test(objs);
    }

    public static void test(Object[] arr) {
        System.out.println(arr);
    }
}
