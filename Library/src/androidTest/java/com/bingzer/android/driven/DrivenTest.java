package com.bingzer.android.driven;

import android.test.AndroidTestCase;

import com.bingzer.android.driven.contracts.Result;
import com.bingzer.android.driven.contracts.Task;
import com.bingzer.android.driven.utils.DriveUtils;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import dagger.ObjectGraph;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DrivenTest extends AndroidTestCase{

    private Driven driven;
    private GoogleAccountCredential credential;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Explicitly set the Dexmaker cache, so tests that use mockito work
        final String dexCache = getContext().getCacheDir().getPath();
        System.setProperty("dexmaker.dexcache", dexCache);

        driven = ObjectGraph.create(StubModule.class).get(Driven.class);
        credential = DriveUtils.createGoogleAccountCredential(getContext(), "TestUserCredential");
    }

    /**
     * Make sure that we have an instance of
     * GoogleDriveService provider when calling by Driven.getDriven()
     * for public API access
     */
    public void test_DrivenAccessForPublic(){
        Driven driven = Driven.getDriven();
        assertTrue(driven.getServiceProvider() instanceof GoogleDriveServiceProvider);
    }

    public void test_DrivenAccessForTest(){
        assertTrue(driven.getServiceProvider() instanceof StubServiceProvider);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    public void test_authenticate(){
        Result<DrivenException> result = driven.authenticate(credential);

        assertTrue(driven.isAuthenticated());
        assertTrue(result.isSuccess());
        assertNull(result.getException());
    }

    public void test_authenticateAsync() throws InterruptedException {
        final CountDownLatch signal = new CountDownLatch(1);
        driven.authenticateAsync(credential, new Task<Result<DrivenException>>() {
            @Override
            public void onCompleted(Result<DrivenException> result) {
                assertTrue(driven.isAuthenticated());
                assertTrue(result.isSuccess());
                assertNull(result.getException());
                signal.countDown();
            }
        });
        signal.await();
    }

    public void test_deauthenticate(){
        driven.authenticate(credential);
        assertTrue(driven.isAuthenticated());

        driven.deauthenticate(getContext());
        assertFalse(driven.isAuthenticated());
    }


    public void test_deauthenticateAsync() throws InterruptedException {
        driven.authenticate(credential);
        assertTrue(driven.isAuthenticated());

        final CountDownLatch signal = new CountDownLatch(1);
        driven.deauthenticateAsync(getContext(), new Task<Result<DrivenException>>() {
            @Override
            public void onCompleted(Result<DrivenException> result) {
                assertFalse(driven.isAuthenticated());
                signal.countDown();
            }
        });
        signal.await();
    }

    public void test_authenticate_NoSave(){
        Result<DrivenException> result = driven.authenticate(DriveUtils.createGoogleAccountCredential(getContext(), null), false);

        assertTrue(driven.isAuthenticated());
        assertTrue(result.isSuccess());
        assertNull(result.getException());
    }

    public void test_getDrivenUser() throws Exception{
        driven.authenticate(credential);

        assertEquals("Name", driven.getDrivenUser().getName());
        assertEquals("DisplayName", driven.getDrivenUser().getDisplayName());
        assertEquals("EmailAddress", driven.getDrivenUser().getEmailAddress());
    }

    public void test_getDrivenService(){
        try {
            assertNull(driven.getDrivenService());
            fail("Should throw exception");
        }
        catch (DrivenException e){
            // -- ignore
        }

        driven.authenticate(credential);
        assertNotNull(driven.getDrivenService());
    }

    public void test_getSharedWithMe(){
        assertNotNull(driven.getShared());
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    public void test_async_fail() throws Exception {
        // we don't authenticate now it should throw error
        final CountDownLatch signal = new CountDownLatch(1);
        driven.titleAsync("Title01", new Task.WithErrorReporting<DrivenFile>() {
            @Override
            public void onCompleted(DrivenFile result) {
                fail("Should throw error");
                signal.countDown();
            }

            @Override
            public void onError(Throwable error) {
                signal.countDown();
            }
        });
        signal.await();
    }

    public void test_get_notAuthenticated() {
        try {
            driven.get("Id01");
            fail("Should throw exception");
        } catch (DrivenException e) {
            // -- ignore
        }
    }

    public void test_get() throws Exception{
        driven.authenticate(credential);
        DrivenFile drivenFile = driven.get("Id01");

        assertNotNull(drivenFile);
        assertEquals("Id01", drivenFile.getId());
        assertEquals("Title01", drivenFile.getTitle());
        assertEquals("MimeType01", drivenFile.getType());
        assertEquals("DownloadUrl01", drivenFile.getDownloadUrl());
        assertFalse(drivenFile.hasDetails());

        // check raw model
        assertEquals("Id01", drivenFile.getModel().getId());
        assertEquals("Description01", drivenFile.getModel().getDescription());
        assertEquals("MimeType01", drivenFile.getModel().getMimeType());
        assertEquals("DownloadUrl01", drivenFile.getModel().getDownloadUrl());
    }

    public void test_getAsync() throws Exception {
        driven.authenticate(credential);
        final CountDownLatch signal = new CountDownLatch(1);
        driven.getAsync("Id03", new Task<DrivenFile>() {
            @Override
            public void onCompleted(DrivenFile drivenFile) {
                assertNotNull(drivenFile);
                assertEquals("Id03", drivenFile.getId());
                assertEquals("Title03", drivenFile.getTitle());
                assertEquals("MimeType03", drivenFile.getType());
                assertEquals("DownloadUrl03", drivenFile.getDownloadUrl());
                assertFalse(drivenFile.hasDetails());

                // check raw model
                assertEquals("Id03", drivenFile.getModel().getId());
                assertEquals("Description03", drivenFile.getModel().getDescription());
                assertEquals("MimeType03", drivenFile.getModel().getMimeType());
                assertEquals("DownloadUrl03", drivenFile.getModel().getDownloadUrl());
                signal.countDown();
            }
        });
        signal.await();
    }

    public void test_title() throws Exception {
        driven.authenticate(credential);
        DrivenFile drivenFile = driven.title("Title02");

        assertNotNull(drivenFile);
        assertEquals("Id02", drivenFile.getId());
        assertEquals("Title02", drivenFile.getTitle());
        assertEquals("MimeType02", drivenFile.getType());
        assertEquals("DownloadUrl02", drivenFile.getDownloadUrl());
        assertFalse(drivenFile.hasDetails());

        // check raw model
        assertEquals("Id02", drivenFile.getModel().getId());
        assertEquals("Description02", drivenFile.getModel().getDescription());
        assertEquals("MimeType02", drivenFile.getModel().getMimeType());
        assertEquals("DownloadUrl02", drivenFile.getModel().getDownloadUrl());
    }

    public void test_titleAsync() throws Exception {
        driven.authenticate(credential);
        final CountDownLatch signal = new CountDownLatch(1);
        driven.titleAsync("Title01", new Task.WithErrorReporting<DrivenFile>() {
            @Override
            public void onCompleted(DrivenFile result) {
                assertNotNull(result);
                assertEquals("Id01", result.getId());
                assertEquals("Title01", result.getTitle());
                assertEquals("MimeType01", result.getType());
                assertEquals("DownloadUrl01", result.getDownloadUrl());
                assertFalse(result.hasDetails());

                // check raw model
                assertEquals("Id01", result.getModel().getId());
                assertEquals("Description01", result.getModel().getDescription());
                assertEquals("MimeType01", result.getModel().getMimeType());
                assertEquals("DownloadUrl01", result.getModel().getDownloadUrl());
                signal.countDown();
            }

            @Override
            public void onError(Throwable error) {
                fail(error.getMessage());
                signal.countDown();
            }
        });
        signal.await();
    }

    public void test_update() throws Exception {
        driven.authenticate(credential);
        DrivenFile drivenFile = driven.get("Id01");
        assertNotNull(drivenFile);

        FileContent fileContent = new FileContent("MimeTypeEdited01", new java.io.File(""));

        driven.update(drivenFile, fileContent);
        drivenFile = driven.get("Id01");

        assertNotNull(drivenFile);
        assertEquals("Id01", drivenFile.getId());
        assertEquals("Title01", drivenFile.getTitle());
        assertEquals("MimeTypeEdited01", drivenFile.getType());  // we changed this (was MimeType01)
        assertEquals("DownloadUrl01", drivenFile.getDownloadUrl());
        assertFalse(drivenFile.hasDetails());

        // check raw model
        assertEquals("Id01", drivenFile.getModel().getId());
        assertEquals("Description01", drivenFile.getModel().getDescription());
        assertEquals("MimeTypeEdited01", drivenFile.getModel().getMimeType());
        assertEquals("DownloadUrl01", drivenFile.getModel().getDownloadUrl());
    }

    public void test_updateAsync() throws Exception{
        driven.authenticate(credential);
        DrivenFile drivenFile = driven.get("Id03");
        assertNotNull(drivenFile);

        final FileContent fileContent = new FileContent("MimeTypeEdited03", new java.io.File(""));
        final CountDownLatch signal = new CountDownLatch(1);
        driven.updateAsync(drivenFile, fileContent, new Task.WithErrorReporting<DrivenFile>() {
            @Override
            public void onCompleted(DrivenFile result) {
                assertNotNull(result);
                assertEquals("Id03", result.getId());
                assertEquals("Title03", result.getTitle());
                assertEquals("MimeTypeEdited03", result.getType());  // we changed this (was MimeType03)
                assertEquals("DownloadUrl03", result.getDownloadUrl());
                assertFalse(result.hasDetails());

                // check raw model
                assertEquals("Id03", result.getModel().getId());
                assertEquals("Description03", result.getModel().getDescription());
                assertEquals("MimeTypeEdited03", result.getModel().getMimeType());
                assertEquals("DownloadUrl03", result.getModel().getDownloadUrl());
                signal.countDown();
            }

            @Override
            public void onError(Throwable error) {
                fail(error.getMessage());
                signal.countDown();
            }
        });
        signal.await();
    }

    public void test_delete() throws Exception {
        driven.authenticate(credential);
        DrivenFile drivenFile = driven.get("Id03");
        assertNotNull(drivenFile);

        assertTrue(driven.delete("Id03"));
    }

    public void test_deleteAsync() throws Exception {
        driven.authenticate(credential);
        DrivenFile drivenFile = driven.get("Id02");
        assertNotNull(drivenFile);

        final CountDownLatch signal = new CountDownLatch(1);
        driven.deleteAsync("Id02", new Task<Boolean>() {
            @Override
            public void onCompleted(Boolean result) {
                assertTrue(result);
                signal.countDown();
            }
        });
        signal.await();
    }

    public void test_first() throws Exception {
        driven.authenticate(credential);
        DrivenFile drivenFile = driven.first("title = 'Title01'");
        assertNotNull(drivenFile);

        assertEquals("Id01", drivenFile.getId());
        assertEquals("Title01", drivenFile.getTitle());
        assertEquals("MimeType01", drivenFile.getType());  // we changed this (was MimeType03)
        assertEquals("DownloadUrl01", drivenFile.getDownloadUrl());
        assertFalse(drivenFile.hasDetails());
    }

    public void test_firstAsync() throws Exception {
        driven.authenticate(credential);

        final CountDownLatch signal = new CountDownLatch(1);
        driven.firstAsync("title = 'Title01'", new Task<DrivenFile>() {
            @Override
            public void onCompleted(DrivenFile result) {
                assertNotNull(result);

                assertEquals("Id01", result.getId());
                assertEquals("Title01", result.getTitle());
                assertEquals("MimeType01", result.getType());  // we changed this (was MimeType03)
                assertEquals("DownloadUrl01", result.getDownloadUrl());
                assertFalse(result.hasDetails());
                signal.countDown();
            }
        });
        signal.await();
    }


    public void test_query() throws Exception {
        driven.authenticate(credential);
        Iterable<DrivenFile> drivenFiles = driven.query("title = 'Title01'");
        for(DrivenFile drivenFile : drivenFiles){
            assertNotNull(drivenFile);

            assertEquals("Id01", drivenFile.getId());
            assertEquals("Title01", drivenFile.getTitle());
            assertEquals("MimeType01", drivenFile.getType());  // we changed this (was MimeType03)
            assertEquals("DownloadUrl01", drivenFile.getDownloadUrl());
            assertFalse(drivenFile.hasDetails());
        }
    }

    public void test_queryAsync() throws Exception {
        driven.authenticate(credential);

        final CountDownLatch signal = new CountDownLatch(1);
        driven.queryAsync("title = 'Title01'", new Task<Iterable<DrivenFile>>() {
            @Override
            public void onCompleted(Iterable<DrivenFile> result) {
                for(DrivenFile drivenFile : result){
                    assertNotNull(drivenFile);

                    assertEquals("Id01", drivenFile.getId());
                    assertEquals("Title01", drivenFile.getTitle());
                    assertEquals("MimeType01", drivenFile.getType());  // we changed this (was MimeType03)
                    assertEquals("DownloadUrl01", drivenFile.getDownloadUrl());
                    assertFalse(drivenFile.hasDetails());
                }
                signal.countDown();
            }
        });
        signal.await();
    }

}
