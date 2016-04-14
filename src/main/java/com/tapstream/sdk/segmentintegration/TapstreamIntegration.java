package com.tapstream.sdk.segmentintegration;

import com.segment.analytics.Analytics;
import com.segment.analytics.ValueMap;
import com.segment.analytics.integrations.Integration;
import com.segment.analytics.integrations.Logger;
import com.segment.analytics.integrations.TrackPayload;
import com.tapstream.sdk.Config;
import com.tapstream.sdk.Event;
import com.tapstream.sdk.Logging;
import com.tapstream.sdk.Tapstream;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Date: 2016-04-12
 * Time: 1:16 PM
 */
public class TapstreamIntegration extends Integration<Tapstream> {
    static final String TAPSTREAM = "Tapstream";

    static void updateSettings(Config conf, ValueMap settings){
        conf.setInstallEventName(settings.getString("installEventName"));
        conf.setOpenEventName(settings.getString("openEventName"));
        conf.setFireAutomaticInstallEvent(settings.getBoolean("fireAutomaticInstallEvent", true));
        conf.setFireAutomaticOpenEvent(settings.getBoolean("fireAutomaticOpenEvent", true));
        conf.setCollectAdvertisingId(settings.getBoolean("collectAdvertisingId", true));
        conf.setOdin1(settings.getString("odin1"));
        conf.setOpenUdid(settings.getString("openUdid"));
        conf.setDeviceId(settings.getString("deviceId"));
        conf.setWifiMac(settings.getString("wifiMac"));
        conf.setAndroidId(settings.getString("androidId"));

        List<String> topLevelKeys = Arrays.asList(
                "accountName", "sdkSecret",
            "installEventName", "openEventName", "fireAutomaticInstallEvent",
            "fireAutomaticOpenEvent", "collectAdvertisingId", "odin1",
            "openUdid", "deviceId", "wifiMac", "androidId"
        );

        for(Map.Entry<String, Object> e: settings.entrySet()){
            if(!topLevelKeys.contains(e.getKey())){
                conf.globalEventParams.put(e.getKey(), e.getValue());
            }
        }
    }

    public static final Factory FACTORY = new Factory() {
        @Override
        public Integration<?> create(ValueMap settings, Analytics analytics) {
            Config conf = new Config();
            updateSettings(conf, settings);

            // Wrap Tapstream logger
            final Logger logger = analytics.logger(TAPSTREAM);
            Logging.setLogger(new com.tapstream.sdk.Logger() {
                @Override
                public void log(int logLevel, String msg) {
                    switch(logLevel){
                        case Logging.ERROR:
                            logger.error(new Exception(msg), msg);
                            break;
                        default:
                            logger.info(msg);
                    }
                }
            });

            String sdkSecret = settings.getString("sdkSecret");
            String accountName = settings.getString("accountName");

            Tapstream.create(analytics.getApplication(), accountName, sdkSecret, conf);
            return new TapstreamIntegration();
        }

        @Override
        public String key() {
            return TAPSTREAM;
        }
    };

    public TapstreamIntegration(){ }

    @Override public void track(TrackPayload payload) {
        Event event = new Event(payload.event(), false);

        for (Map.Entry<String, Object> e: payload.properties().entrySet()) {
            event.addPair(e.getKey(), e.getValue().toString());
        }
        Tapstream.getInstance().fireEvent(event);
    }
}
