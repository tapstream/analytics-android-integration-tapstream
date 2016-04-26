package com.tapstream.sdk.segmentintegration;

import android.app.Application;
import android.content.SharedPreferences;


import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;
import com.segment.analytics.ValueMap;
import com.segment.analytics.integrations.Logger;
import com.segment.analytics.test.TrackPayloadBuilder;
import com.tapstream.sdk.Event;
import com.tapstream.sdk.Platform;
import com.tapstream.sdk.Tapstream;

import org.assertj.core.data.MapEntry;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyNoMoreInteractions;

/**
 * Date: 2016-04-12
 * Time: 2:07 PM
 */


@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 18)
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*", "org.json.*" })
@PrepareForTest(Tapstream.class)
public class TapstreamIntegrationTest {
    @Mock Analytics analytics;
    @Mock Tapstream tapstream;
    @Mock Application context;
    @Mock SharedPreferences prefs;
    @Mock Platform platform;
    @Rule public PowerMockRule rule = new PowerMockRule();

    Logger logger;
    static UUID uuid = UUID.randomUUID();
    com.tapstream.sdk.Config config = new com.tapstream.sdk.Config();

    TapstreamIntegration integration;

    @Before
    public void setUp() throws Exception{
        initMocks(this);
        mockStatic(Tapstream.class);
        doNothing().when(Tapstream.class, "create", any(), anyString(), anyString(), any());
        doReturn(tapstream).when(Tapstream.class, "getInstance");
        Tapstream ts = Tapstream.getInstance();


        logger = Logger.with(Analytics.LogLevel.DEBUG);
        when(analytics.logger("Tapstream")).thenReturn(logger);
        when(analytics.getApplication()).thenReturn(context);
        when(context.getApplicationContext()).thenReturn(context);

        /*
        // Satisfy PlatformImpl's curiousity.

        // PlatformImpl.loadUuid

        SharedPreferences uuidPrefs = mock(SharedPreferences.class);
        when(context.getSharedPreferences("TapstreamSDKUUID", Context.MODE_PRIVATE)).thenReturn(uuidPrefs);
        when(uuidPrefs.getString("uuid", null)).thenReturn(uuid.toString());
        when(uuidPrefs.getString("referrer", null)).thenReturn("http://somereferrer.com/");
        when(uuidPrefs.getString("advertisingId", null)).thenReturn("android-advertising-id");
        when(uuidPrefs.contains("limitAdTracking")).thenReturn(false);

        // loadFiredEvents
        SharedPreferences firedEventsPrefs = mock(SharedPreferences.class);
        when(context.getSharedPreferences("TapstreamSDKFiredEvents", Context.MODE_PRIVATE)).thenReturn(firedEventsPrefs);
        when(firedEventsPrefs.getAll()).thenReturn(Collections.EMPTY_MAP);

        // PlatformImpl.getResolution
        WindowManager wm = mock(WindowManager.class);
        when(wm.getDefaultDisplay()).thenReturn(mock(Display.class));
        when(context.getSystemService(Context.WINDOW_SERVICE)).thenReturn(wm);

        // PlatformImpl.getAppName (and getAppVersion)
        String PACKAGE_NAME = "com.tapstream.sdk.sdktest";
        when(context.getPackageName()).thenReturn(PACKAGE_NAME);
        PackageManager pm = mock(PackageManager.class);
        when(context.getPackageManager()).thenReturn(pm);

        try {
            when(pm.getApplicationInfo(PACKAGE_NAME, 0)).thenThrow(new PackageManager.NameNotFoundException());
            when(pm.getPackageInfo(PACKAGE_NAME, 0)).thenThrow(new PackageManager.NameNotFoundException());
        }catch(PackageManager.NameNotFoundException e) {
            fail(e.getMessage());
        }

*/
        ValueMap settings = new ValueMap()
                .putValue("accountName", "sdktest")
                .putValue("sdkSecret", "sdksecret");

        integration =
            (TapstreamIntegration) TapstreamIntegration.FACTORY.create(settings, analytics);


        //doReturn(tapstream).when(Tapstream.class, "getInstance");


    }

    @Test public void testUpdateEventNameSettings(){
        com.tapstream.sdk.Config conf = new com.tapstream.sdk.Config();

        ValueMap settings = new ValueMap()
                .putValue("accountName", "sdktest")
                .putValue("installEventName", "my-install-event")
                .putValue("openEventName", "my-open-event");

        assertThat(conf.getInstallEventName()).isNull();
        assertThat(conf.getOpenEventName()).isNull();

        TapstreamIntegration.updateSettings(conf, settings);

        assertThat(conf.getInstallEventName()).isEqualTo("my-install-event");
        assertThat(conf.getOpenEventName()).isEqualTo("my-open-event");
    }

    @Test public void testUpdateHardwareValues() {
        com.tapstream.sdk.Config conf = new com.tapstream.sdk.Config();

        ValueMap settings = new ValueMap()
                .putValue("accountName", "sdktest")
                .putValue("odin1", "my-odin1")
                .putValue("openUdid", "my-openUdid")
                .putValue("wifiMac", "my-wifiMac")
                .putValue("deviceId", "my-deviceId")
                .putValue("androidId", "my-androidId");

        assertThat(conf.getOdin1()).isNull();
        assertThat(conf.getOpenUdid()).isNull();
        assertThat(conf.getWifiMac()).isNull();
        assertThat(conf.getDeviceId()).isNull();
        assertThat(conf.getAndroidId()).isNull();

        TapstreamIntegration.updateSettings(conf, settings);

        assertThat(conf.getOdin1()).isEqualTo("my-odin1");
        assertThat(conf.getOpenUdid()).isEqualTo("my-openUdid");
        assertThat(conf.getWifiMac()).isEqualTo("my-wifiMac");
        assertThat(conf.getDeviceId()).isEqualTo("my-deviceId");
        assertThat(conf.getAndroidId()).isEqualTo("my-androidId");
    }
    @Test public void testUpdateAutomaticEvents() {
        com.tapstream.sdk.Config conf = new com.tapstream.sdk.Config();

        ValueMap settings = new ValueMap()
                .putValue("accountName", "sdktest")
                .putValue("fireAutomaticInstallEvent", false)
                .putValue("fireAutomaticOpenEvent", false);

        assertThat(conf.getFireAutomaticInstallEvent()).isTrue();
        assertThat(conf.getFireAutomaticOpenEvent()).isTrue();

        TapstreamIntegration.updateSettings(conf, settings);

        assertThat(conf.getFireAutomaticInstallEvent()).isFalse();
        assertThat(conf.getFireAutomaticOpenEvent()).isFalse();
    }

    @Test public void testCollectAdvertisingId(){
        com.tapstream.sdk.Config conf = new com.tapstream.sdk.Config();
        ValueMap settings = new ValueMap()
                .putValue("accountName", "sdktest")
                .putValue("collectAdvertisingId", false);

        assertThat(conf.getCollectAdvertisingId()).isTrue();

        TapstreamIntegration.updateSettings(conf, settings);
        assertThat(conf.getCollectAdvertisingId()).isFalse();

        settings.putValue("collectAdvertisingId", true);
        TapstreamIntegration.updateSettings(conf, settings);
        assertThat(conf.getCollectAdvertisingId()).isTrue();

    }

    @Test public void testSetGlobalParamValues(){
        com.tapstream.sdk.Config conf = new com.tapstream.sdk.Config();
        ValueMap settings = new ValueMap()
                .putValue("accountName", "sdktest")
                .putValue("my-key", "my-value")
                .putValue("my-other-key", "my-other-value");

        assertThat(conf.globalEventParams).isEmpty();

        TapstreamIntegration.updateSettings(conf, settings);

        System.out.println(String.format("GLOBAL EVENT PARAMS LEN: %d", conf.globalEventParams.size()));

        assertThat(conf.globalEventParams).containsExactly(
                MapEntry.entry("my-key", "my-value"),
                MapEntry.entry("my-other-key", "my-other-value")
        );
    }

    @Test public void testTrack(){
        ValueMap settings = new ValueMap().putValue("accountName", "sdktest");
        TapstreamIntegration integration =
                (TapstreamIntegration) TapstreamIntegration.FACTORY.create(settings, analytics);

        //verifyStatic();
        Tapstream ts = Tapstream.getInstance();

        verify(ts, times(0)).fireEvent((Event) any());

        integration.track(new TrackPayloadBuilder().event("test-event").build());
        verify(ts, times(1)).fireEvent(eventEq(new Event("test-event", false)));
    }

    @Test public void testTrackWithParameters(){
        ValueMap settings = new ValueMap().putValue("accountName", "sdktest");
        TapstreamIntegration integration =
                (TapstreamIntegration) TapstreamIntegration.FACTORY.create(settings, analytics);

        Tapstream ts = Tapstream.getInstance();

        verify(ts, times(0)).fireEvent((Event) any());

        Event expected = new Event("test-event", false);
        expected.addPair("name", "test-value");

        Properties props = new Properties();
        props.put("name", "test-value");

        integration.track(new TrackPayloadBuilder().event("test-event").properties(props).build());

        verify(ts, times(1)).fireEvent(eventEq(expected));

    }

    public static Event eventEq(Event expected){
        return argThat(new EventMatcher(expected));
    }
    static class EventMatcher extends TypeSafeMatcher<Event> {
        Event event;
        EventMatcher(Event event){
            this.event = event;
        }
        @Override
        protected boolean matchesSafely(Event other) {
            return this.event.getName().equals(other.getName())
                   && this.event.getPostData().equals(other.getPostData());
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(event.toString());
        }
    }
}
