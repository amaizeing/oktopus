package test.response.raw;

import com.amaizeing.oktopus.annotation.OktopusDependOn;
import com.amaizeing.oktopus.annotation.OktopusRequestUrl;
import com.amaizeing.oktopus.annotation.method.GetRequest;

public class RequestDependence {


    @GetRequest
    static final class Request01 {

        @OktopusRequestUrl
        public String url() {
            return null;
        }

    }

    @GetRequest
    static final class Request02 {

        @OktopusRequestUrl
        public String url() {
            return null;
        }

    }

    @GetRequest
    static final class Request03 {

        @OktopusRequestUrl
        public String url() {
            return null;
        }

    }

    @GetRequest
    static final class Request04 {

        @OktopusDependOn(Request01.class)
        byte[] request01Response;

        @OktopusRequestUrl
        public String url() {
            return null;
        }

    }

    @GetRequest
    static final class Request05 {

        @OktopusDependOn(Request01.class)
        byte[] request01Response;

        @OktopusRequestUrl
        public String url() {
            return null;
        }

    }

    @GetRequest
    static final class Request06 {

        @OktopusDependOn(Request04.class)
        byte[] request04Response;

        @OktopusDependOn(Request05.class)
        byte[] request05Response;

        @OktopusRequestUrl
        public String url() {
            return null;
        }

    }

    @GetRequest
    static final class Request07 {

        @OktopusDependOn(Request02.class)
        byte[] request02Response;

        @OktopusRequestUrl
        public String url() {
            return null;
        }

    }

    @GetRequest
    static final class Request08 {

        @OktopusDependOn(Request06.class)
        byte[] request06Response;

        @OktopusDependOn(Request07.class)
        byte[] request07Response;

        @OktopusDependOn(Request09.class)
        byte[] request09Response;

        @OktopusRequestUrl
        public String url() {
            return null;
        }

    }

    @GetRequest
    static final class Request09 {

        @OktopusDependOn(Request03.class)
        byte[] request03Response;

        @OktopusRequestUrl
        public String url() {
            return null;
        }

    }

    @GetRequest
    static final class Request10 {

        @OktopusDependOn(Request08.class)
        byte[] request08Response;

        @OktopusDependOn(Request11.class)
        byte[] request11Response;

        @OktopusRequestUrl
        public String url() {
            return null;
        }

    }

    @GetRequest
    static final class Request11 {

        @OktopusRequestUrl
        public String url() {
            return null;
        }

    }

    @GetRequest
    static final class Request12 {

        @OktopusDependOn(Request10.class)
        byte[] request10Response;

        @OktopusRequestUrl
        public String url() {
            return null;
        }

    }

    @GetRequest
    static final class Request13 {

        @OktopusDependOn(Request06.class)
        byte[] request06Response;

        @OktopusDependOn(Request12.class)
        byte[] request12Response;

        @OktopusRequestUrl
        public String url() {
            return null;
        }

    }

}
