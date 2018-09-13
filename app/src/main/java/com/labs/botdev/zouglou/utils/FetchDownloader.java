package com.labs.botdev.zouglou.utils;

import android.annotation.SuppressLint;
import android.content.Context;

import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.Error;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchConfiguration;
import com.tonyodev.fetch2.FetchListener;
import com.tonyodev.fetch2.NetworkType;
import com.tonyodev.fetch2.Priority;
import com.tonyodev.fetch2.Request;
import com.tonyodev.fetch2core.DownloadBlock;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FetchDownloader {
    private Fetch fetch;

    @SuppressLint("CheckResult")
    public String downloadFile(Context ctx, String url, String outputFile) {
        FetchConfiguration fetchConfiguration = new FetchConfiguration.Builder(ctx)
                .setDownloadConcurrentLimit(3)
                .build();

        fetch = Fetch.Impl.getInstance(fetchConfiguration);

//        url = "http://www.example.com/test.txt";
        String file = outputFile;

        final Request request = new Request(url, file);
        request.setPriority(Priority.HIGH);
        request.setNetworkType(NetworkType.ALL);
        //request.addHeader("Authorization", "JWT "+AppController.getInstance().getToken());

        fetch.enqueue(request, updatedRequest -> {
            //Request was successfully enqueued for download.
        }, error -> {
            //An error occurred enqueuing the request.
        });

        FetchListener fetchListener = new FetchListener() {
            @Override
            public void onWaitingNetwork(Download download) {

            }

            @Override
            public void onStarted(Download download, List<? extends DownloadBlock> list, int i) {

            }

            @Override
            public void onError(Download download, Error error, Throwable throwable) {

            }

            @Override
            public void onDownloadBlockUpdated(Download download, DownloadBlock downloadBlock, int i) {

            }

            @Override
            public void onAdded(Download download) {

            }

            @Override
            public void onQueued(@NotNull Download download, boolean waitingOnNetwork) {
                if (request.getId() == download.getId()) {

                }
            }

            @Override
            public void onCompleted(@NotNull Download download) {

            }


            @Override
            public void onProgress(@NotNull Download download, long etaInMilliSeconds, long downloadedBytesPerSecond) {
                if (request.getId() == download.getId()) {
                    //updateDownload(download, etaInMilliSeconds);
                }
                int progress = download.getProgress();
            }

            @Override
            public void onPaused(@NotNull Download download) {

            }

            @Override
            public void onResumed(@NotNull Download download) {

            }

            @Override
            public void onCancelled(@NotNull Download download) {

            }

            @Override
            public void onRemoved(@NotNull Download download) {

            }

            @Override
            public void onDeleted(@NotNull Download download) {

            }
        };
        fetch.addListener(fetchListener);
        return file;
    }

}

