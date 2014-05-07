/**
 * Copyright 2014 Ricky Tobing
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance insert the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bingzer.android.driven.contracts;

import com.bingzer.android.driven.DriveFile;
import com.bingzer.android.driven.DrivenException;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;

import java.io.File;

/**
 * Created by Ricky on 5/3/2014.
 */
@SuppressWarnings("unused")
public interface DrivenApi {

    public static interface Auth {

        Result<DrivenException> authenticate(GoogleAccountCredential credential);

        Result<DrivenException> authenticate(GoogleAccountCredential credential, boolean saveCredential);

        void authenticateAsync(GoogleAccountCredential credential, Task<Result<DrivenException>> result);

        void authenticateAsync(GoogleAccountCredential credential, boolean saveCredential, Task<Result<DrivenException>> result);
    }

    public static interface Get {

        DriveFile get(String id);

        void getAsync(String id, Task<DriveFile> result);

        public static interface ByTitle {

            DriveFile title(DriveFile parent, String title);

            DriveFile title(String title);

            void titleAsync(DriveFile parent, String title, Task<DriveFile> result);

            void titleAsync(String title, Task<DriveFile> result);
        }

    }

    public static interface Details {

        DriveFile getDetails(DriveFile driveFile);

        void getDetailsAsync(DriveFile driveFile, Task<DriveFile> result);

    }

    public static interface List {

        Iterable<DriveFile> list();

        Iterable<DriveFile> list(DriveFile folder);

        void listAsync(DriveFile folder, Task<Iterable<DriveFile>> result);

        void listAsync(Task<Iterable<DriveFile>> result);

    }

    public static interface Post {

        /**
         * Creates on the root
         * @param name
         * @return
         */
        DriveFile create(String name);

        DriveFile create(String name, FileContent content);

        DriveFile create(DriveFile parent, String name);

        DriveFile create(DriveFile parent, String name, FileContent content);

        void createAsync(DriveFile parent, String name, FileContent content, Task<DriveFile> result);

        void createAsync(DriveFile parent, String name, Task<DriveFile> result);

        void createAsync(String name, Task<DriveFile> result);

        void createAsync(String name, FileContent content, Task<DriveFile> result);

    }

    public static interface Put {

        DriveFile update(DriveFile driveFile, FileContent content);

        void updateAsync(DriveFile driveFile, FileContent content, Task<DriveFile> result);
    }

    public static interface Delete {

        boolean delete(String id);

        void deleteAsync(String id, Task<Boolean> result);

    }

    public static interface Query {

        DriveFile first(String query);

        void firstAsync(String query, Task<DriveFile> result);

        Iterable<DriveFile> query(String query);

        void queryAsync(String query, Task<Iterable<DriveFile>> result);

    }

    public static interface Download {

        File download(DriveFile driveFile, File local);

        void downloadAsync(DriveFile driveFile, File local, Task<File> result);

    }

    public static interface Share {

        boolean share(DriveFile driveFile, String user);

        void shareAsync(DriveFile driveFile, String user, Task<Boolean> result);

    }

}
