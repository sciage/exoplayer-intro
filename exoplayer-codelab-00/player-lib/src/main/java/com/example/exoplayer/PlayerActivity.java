/*
 * Copyright (C) 2017 The Android Open Source Project
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
package com.example.exoplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;


/**
 * A fullscreen activity to play audio or video streams.
 */
public class PlayerActivity extends AppCompatActivity {
  private PlayerView playerView;
  private SimpleExoPlayer player;

  private boolean playWhenReady = true;
  private int currentWindow = 0;
  private long playbackPosition = 0;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_player);
    playerView = findViewById(R.id.video_view);

  }

  //supply the state information you saved in releasePlayer to your player during initialization.
  private void initializePlayer(){

    if (player == null){
      // First, create a DefaultTrackSelector, which is responsible for choosing tracks in the media item.
      DefaultTrackSelector trackSelector = new DefaultTrackSelector(this);
      // Then, tell your trackSelector to only pick tracks of standard definition or
      // lower—a good way of saving your user's data at the expense of quality.
      trackSelector.setParameters(trackSelector.buildUponParameters().setMaxVideoSizeSd()); // max video height width
      // Lastly, pass your trackSelector to your builder so that it is used when building
      // the SimpleExoPlayer instance.
      player = new SimpleExoPlayer.Builder(this).setTrackSelector(trackSelector).build();
    }
//    player = new SimpleExoPlayer.Builder(this).build();
    playerView.setPlayer(player);
/*
    MediaItem mediaItem = MediaItem.fromUri(getString(R.string.media_url_mp3));
    player.setMediaItem(mediaItem);

    // second mediaItem added
    MediaItem secondMediaItem = MediaItem.fromUri(getString(R.string.media_url_mp3));
    player.addMediaItem(secondMediaItem);

 */

    MediaItem mediaItem = new MediaItem.Builder()
                          .setUri(getString(R.string.media_url_dash))
                          .setMimeType(MimeTypes.APPLICATION_MPD)
                          .build();
    player.setMediaItem(mediaItem);

    //setPlayWhenReady tells the player whether to start playing as soon as all resources
    // for playback have been acquired.
    player.setPlayWhenReady(playWhenReady);
    // seekTo tells the player to seek to a certain position within a specific window.
    // Both currentWindow and playbackPosition are initialized to zero so that playback
    // starts from the very start the first time the app is run.
    player.seekTo(currentWindow, playbackPosition);
    // prepare tells the player to acquire all the resources required for playback.
    player.prepare();
  }

  //Android API level 24 and higher supports multiple windows. As your app can be visible,
  // but not active in split window mode, you need to initialize the player in onStart
  @Override
  protected void onStart() {
    super.onStart();
    if (Util.SDK_INT > 24){
      initializePlayer();
    }
  }

  //Android API level 24 and lower requires you to wait as long as possible until you grab resources,
  // so you wait until onResume before initializing the player.
  @Override
  protected void onResume() {
    super.onResume();
    hideSystemUi();
    if (Util.SDK_INT < 24 || player == null){
      initializePlayer();
    }
  }


  @SuppressLint("InlinedApi")
  private void hideSystemUi() {
    playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
  }

  //With API Level 24 and lower, there is no guarantee of onStop being called,
  // so you have to release the player as early as possible in onPause.
  @Override
  public void onPause() {
    super.onPause();
    if (Util.SDK_INT < 24) {
      releasePlayer();
    }
  }

  //With API Level 24 and higher (which brought multi- and split-window mode),
  // onStop is guaranteed to be called. In the paused state, your activity is still visible,
  // so you wait to release the player until onStop.
  @Override
  public void onStop() {
    super.onStop();
    if (Util.SDK_INT >= 24) {
      releasePlayer();
    }
  }

  // Before you release and destroy the player, store the following information
  // This allows you to resume playback from where the user left off.
  // All you need to do is supply this state information when you initialize your player.
  private void releasePlayer() {
    if (player != null) {
      // Play/pause state using getPlayWhenReady.
      playWhenReady = player.getPlayWhenReady();
      // Current playback position using getCurrentPosition
      playbackPosition = player.getCurrentPosition();
      // Current window index using getCurrentWindowIndex
      currentWindow = player.getCurrentWindowIndex();
      player.release();
      player = null;
    }
  }

}
