package com.khahdihdz.videodownloader
import org.junit.Assert.*;import org.junit.Test
class UrlDetectorTest{
 @Test fun youtube(){assertEquals(VideoPlatform.YOUTUBE,UrlDetector.detect("https://youtube.com/watch?v=1"));assertEquals(VideoPlatform.YOUTUBE,UrlDetector.detect("https://youtu.be/1"));assertEquals(VideoPlatform.YOUTUBE,UrlDetector.detect("https://youtube.com/shorts/1"))}
 @Test fun facebook(){assertEquals(VideoPlatform.FACEBOOK,UrlDetector.detect("https://facebook.com/watch/?v=1"));assertEquals(VideoPlatform.FACEBOOK,UrlDetector.detect("https://facebook.com/reel/1"));assertEquals(VideoPlatform.FACEBOOK,UrlDetector.detect("https://facebook.com/share/v/1"));assertEquals(VideoPlatform.FACEBOOK,UrlDetector.detect("https://m.facebook.com/x"))}
 @Test fun embedded(){assertEquals("https://youtu.be/abc",UrlDetector.extract("Xem https://youtu.be/abc"))}
 @Test fun unknown(){assertEquals(VideoPlatform.UNKNOWN,UrlDetector.detect("https://example.com/x"))}
}