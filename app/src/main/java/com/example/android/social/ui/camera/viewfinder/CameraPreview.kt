/*
 * Copyright (C) 2023 The Android Open Source Project
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

package com.example.android.social.ui.camera.viewfinder

import android.graphics.Bitmap
import android.util.Log
import android.view.Surface
import androidx.camera.core.Preview.SurfaceProvider
import androidx.camera.core.SurfaceRequest
import androidx.camera.view.PreviewView.ImplementationMode
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import com.example.android.social.ui.camera.viewfinder.surface.CombinedSurface
import com.example.android.social.ui.camera.viewfinder.surface.CombinedSurfaceEvent
import com.example.android.social.ui.camera.viewfinder.surface.SurfaceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.flow.mapNotNull

private const val TAG = "Preview"

@Composable
fun CameraPreview(
    modifier: Modifier,
    implementationMode: ImplementationMode = ImplementationMode.COMPATIBLE,
    onSurfaceProviderReady: (SurfaceProvider) -> Unit = {},
    onRequestBitmapReady: (() -> Bitmap?) -> Unit,
) {
    Log.d(TAG, "CameraPreview")

    val surfaceRequest by produceState<SurfaceRequest?>(initialValue = null) {
        onSurfaceProviderReady(
            SurfaceProvider { request ->
                value?.willNotProvideSurface()
                value = request
            },
        )
    }

    PreviewSurface(
        surfaceRequest = surfaceRequest,
    )
}

@Composable
fun PreviewSurface(
    surfaceRequest: SurfaceRequest?,
//    onRequestBitmapReady: (() -> Bitmap?) -> Unit,
    type: SurfaceType = SurfaceType.TEXTURE_VIEW,
    implementationMode: ImplementationMode = ImplementationMode.COMPATIBLE,
) {
    Log.d(TAG, "PreviewSurface")

    var surface: Surface? by remember { mutableStateOf(null) }

    LaunchedEffect(surfaceRequest, surface) {
        Log.d(TAG, "LaunchedEffect")
        snapshotFlow {
            if (surfaceRequest == null || surface == null) {
                null
            } else {
                Pair(surfaceRequest, surface)
            }
        }.mapNotNull { it }
            .collect { (request, surface) ->
                Log.d(TAG, "Collect: Providing surface")

                request.provideSurface(surface!!, Dispatchers.Main.asExecutor()) {}
            }
    }

    when (implementationMode) {
        ImplementationMode.PERFORMANCE -> TODO()
        ImplementationMode.COMPATIBLE -> CombinedSurface(
            onSurfaceEvent = { event ->
                surface = when (event) {
                    is CombinedSurfaceEvent.SurfaceAvailable -> {
                        event.surface
                    }

                    is CombinedSurfaceEvent.SurfaceDestroyed -> {
                        null
                    }
                }
            },
        )
    }
}