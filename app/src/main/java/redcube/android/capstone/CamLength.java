package redcube.android.capstone;

import android.content.DialogInterface;
import android.opengl.GLES30;
import android.os.Bundle;
import android.content.Intent;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.media.Image;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.ArCoreApk.Availability;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Config.InstantPlacementMode;
import com.google.ar.core.DepthPoint;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.InstantPlacementPoint;
import com.google.ar.core.LightEstimate;
import com.google.ar.core.Plane;
import com.google.ar.core.Point;
import com.google.ar.core.Point.OrientationMode;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingFailureReason;
import com.google.ar.core.TrackingState;
import redcube.android.capstone.common.helpers.CameraPermissionHelper;
import redcube.android.capstone.common.helpers.DepthSettings;
import redcube.android.capstone.common.helpers.DisplayRotationHelper;
import redcube.android.capstone.common.helpers.FullScreenHelper;
import redcube.android.capstone.common.helpers.InstantPlacementSettings;
import redcube.android.capstone.common.helpers.SnackbarHelper;
import redcube.android.capstone.common.helpers.TapHelper;
import redcube.android.capstone.common.helpers.TrackingStateHelper;
import redcube.android.capstone.samplerender.Framebuffer;
import redcube.android.capstone.samplerender.Mesh;
import redcube.android.capstone.samplerender.SampleRender;
import redcube.android.capstone.samplerender.Shader;
import redcube.android.capstone.samplerender.Texture;
import redcube.android.capstone.samplerender.VertexBuffer;
import redcube.android.capstone.samplerender.GLError;
import redcube.android.capstone.samplerender.arcore.BackgroundRenderer;
import redcube.android.capstone.samplerender.arcore.PlaneRenderer;
import redcube.android.capstone.samplerender.arcore.SpecularCubemapFilter;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.NotYetAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.Locale;

public class CamLength extends AppCompatActivity implements SampleRender.Renderer {

    private static final String TAG = CamLength.class.getSimpleName();

    private static final String SEARCHING_PLANE_MESSAGE = "Searching for surfaces...";
    private static final String WAITING_FOR_TAP_MESSAGE = "Tap on a surface to place an object.";

    // See the definition of updateSphericalHarmonicsCoefficients for an explanation of these
    // constants.
    private static final float[] sphericalHarmonicFactors = {
            0.282095f,
            -0.325735f,
            0.325735f,
            -0.325735f,
            0.273137f,
            -0.273137f,
            0.078848f,
            -0.273137f,
            0.136569f,
    };

    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 100.0f;
    private static final int MAX_ANCHORS = 3;  // 최대 앵커 개수를 3개로 변경

    private static final int CUBEMAP_RESOLUTION = 16;
    private static final int CUBEMAP_NUMBER_OF_IMPORTANCE_SAMPLES = 32;

    // Rendering. The Renderers are created here, and initialized when the GL surface is created.
    private GLSurfaceView surfaceView;

    private boolean installRequested;

    private Session session;
    private final SnackbarHelper messageSnackbarHelper = new SnackbarHelper();
    private DisplayRotationHelper displayRotationHelper;
    private final TrackingStateHelper trackingStateHelper = new TrackingStateHelper(this);
    private TapHelper tapHelper;
    private SampleRender render;

    private PlaneRenderer planeRenderer;
    private BackgroundRenderer backgroundRenderer;
    private Framebuffer virtualSceneFramebuffer;
    private boolean hasSetTextureNames = false;

    private final DepthSettings depthSettings = new DepthSettings();
    private boolean[] depthSettingsMenuDialogCheckboxes = new boolean[2];

    private final InstantPlacementSettings instantPlacementSettings = new InstantPlacementSettings();
    private boolean[] instantPlacementSettingsMenuDialogCheckboxes = new boolean[1];
    // Assumed distance from the device camera to the surface on which user will try to place objects.
    // This value affects the apparent scale of objects while the tracking method of the
    // Instant Placement point is SCREENSPACE_WITH_APPROXIMATE_DISTANCE.
    // Values in the [0.2, 2.0] meter range are a good choice for most AR experiences. Use lower
    // values for AR experiences where users are expected to place objects on surfaces close to the
    // camera. Use larger values for experiences where the user will likely be standing and trying to
    // place an object on the ground or floor in front of them.
    private static final float APPROXIMATE_DISTANCE_METERS = 2.0f;

    // Point Cloud
    private VertexBuffer pointCloudVertexBuffer;
    private Mesh pointCloudMesh;
    private Shader pointCloudShader;
    // Keep track of the last point cloud rendered to avoid updating the VBO if point cloud
    // was not changed.  Do this using the timestamp since we can't compare PointCloud objects.
    private long lastPointCloudTimestamp = 0;

    // Virtual object (ARCore pawn)
    private Mesh virtualObjectMesh;
    private Shader virtualObjectShader;
    private Texture virtualObjectAlbedoTexture;
    private Texture virtualObjectAlbedoInstantPlacementTexture;

    private final List<WrappedAnchor> wrappedAnchors = new ArrayList<>();

    // Environmental HDR
    private Texture dfgTexture;
    private SpecularCubemapFilter cubemapFilter;

    // Temporary matrix allocated here to reduce number of allocations for each frame.
    private final float[] modelMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] modelViewMatrix = new float[16]; // view x model
    private final float[] modelViewProjectionMatrix = new float[16]; // projection x view x model
    private final float[] sphericalHarmonicsCoefficients = new float[9 * 3];
    private final float[] viewInverseMatrix = new float[16];
    private final float[] worldLightDirection = {0.0f, 0.0f, 0.0f, 0.0f};
    private final float[] viewLightDirection = new float[4]; // view x world light direction

    // 선 렌더링을 위한 변수들
    private Shader lineShader;
    private VertexBuffer lineVertexBuffer;
    private Mesh lineMesh;
    private final float[] lineColor = {0.0f, 1.0f, 0.0f, 1.0f}; // 초록색 선
    private final float lineWidth = 0.02f; // 선의 두께

    // 거리 측정을 위한 변수들
    private TextView distanceTextView;
    private float currentDistance1 = 0.0f;  // 첫 번째 거리 (앵커 1-2)
    private float currentDistance2 = 0.0f;  // 두 번째 거리 (앵커 2-3)
    private static final int AVERAGE_FRAMES = 10;
    private float[] distanceHistory1 = new float[AVERAGE_FRAMES];  // 첫 번째 거리 히스토리
    private float[] distanceHistory2 = new float[AVERAGE_FRAMES];  // 두 번째 거리 히스토리
    private int historyIndex = 0;
    private boolean isHistoryFull = false;

    private String selectedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camlength);
        surfaceView = findViewById(R.id.surfaceview);
        displayRotationHelper = new DisplayRotationHelper(/* context= */ this);

        // 거리 표시를 위한 TextView 초기화
        distanceTextView = findViewById(R.id.distance_text);
        if (distanceTextView == null) {
            distanceTextView = new TextView(this);
            distanceTextView.setTextColor(Color.WHITE);
            distanceTextView.setTextSize(20);
            distanceTextView.setGravity(Gravity.CENTER);
            ((ViewGroup) surfaceView.getParent()).addView(distanceTextView);
        }

        // Set up touch listener.
        tapHelper = new TapHelper(/* context= */ this);
        surfaceView.setOnTouchListener(tapHelper);

        // Set up renderer.
        render = new SampleRender(surfaceView, this, getAssets());

        installRequested = false;

        depthSettings.onCreate(this);
        instantPlacementSettings.onCreate(this);


        selectedItem = getIntent().getStringExtra("selectedCategory");

        // 버튼 초기화
        Button btn = findViewById(R.id.btn1);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CamLength.this, MainActivity3.class);
                // 거리 값을 Intent에 추가
                intent.putExtra("distance1", currentDistance1 * 100.0f);
                intent.putExtra("distance2", currentDistance2 * 100.0f);
                intent.putExtra("selectedCategory", selectedItem);
                startActivity(intent);
                finish();  // AR 세션 정리
            }
        });

    }
    /** Menu button to launch feature specific settings. */


    @Override
    protected void onDestroy() {
        if (session != null) {
            // Explicitly close ARCore Session to release native resources.
            // Review the API reference for important considerations before calling close() in apps with
            // more complicated lifecycle requirements:
            // https://developers.google.com/ar/reference/java/arcore/reference/com/google/ar/core/Session#close()
            session.close();
            session = null;
        }

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (session == null) {
            Exception exception = null;
            String message = null;
            try {
                // Always check the latest availability.
                Availability availability = ArCoreApk.getInstance().checkAvailability(this);

                // In all other cases, try to install ARCore and handle installation failures.
                if (availability != Availability.SUPPORTED_INSTALLED) {
                    switch (ArCoreApk.getInstance().requestInstall(this, !installRequested)) {
                        case INSTALL_REQUESTED:
                            installRequested = true;
                            return;
                        case INSTALLED:
                            break;
                    }
                }

                // ARCore requires camera permissions to operate. If we did not yet obtain runtime
                // permission on Android M and above, now is a good time to ask the user for it.
                if (!CameraPermissionHelper.hasCameraPermission(this)) {
                    CameraPermissionHelper.requestCameraPermission(this);
                    return;
                }

                // Create the session.
                session = new Session(/* context= */ this);
            } catch (UnavailableArcoreNotInstalledException
                     | UnavailableUserDeclinedInstallationException e) {
                message = "Please install ARCore";
                exception = e;
            } catch (UnavailableApkTooOldException e) {
                message = "Please update ARCore";
                exception = e;
            } catch (UnavailableSdkTooOldException e) {
                message = "Please update this app";
                exception = e;
            } catch (UnavailableDeviceNotCompatibleException e) {
                message = "This device does not support AR";
                exception = e;
            } catch (Exception e) {
                message = "Failed to create AR session";
                exception = e;
            }

            if (message != null) {
                messageSnackbarHelper.showError(this, message);
                Log.e(TAG, "Exception creating session", exception);
                return;
            }
        }

        // Note that order matters - see the note in onPause(), the reverse applies here.
        try {
            configureSession();
            // To record a live camera session for later playback, call
            // `session.startRecording(recordingConfig)` at anytime. To playback a previously recorded AR
            // session instead of using the live camera feed, call
            // `session.setPlaybackDatasetUri(Uri)` before calling `session.resume()`. To
            // learn more about recording and playback, see:
            // https://developers.google.com/ar/develop/java/recording-and-playback
            session.resume();
        } catch (CameraNotAvailableException e) {
            messageSnackbarHelper.showError(this, "Camera not available. Try restarting the app.");
            session = null;
            return;
        }

        surfaceView.onResume();
        displayRotationHelper.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (session != null) {
            // Note that the order matters - GLSurfaceView is paused first so that it does not try
            // to query the session. If Session is paused before GLSurfaceView, GLSurfaceView may
            // still call session.update() and get a SessionPausedException.
            displayRotationHelper.onPause();
            surfaceView.onPause();
            session.pause();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            // Use toast instead of snackbar here since the activity will exit.
            Toast.makeText(this, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
                    .show();
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                CameraPermissionHelper.launchPermissionSettings(this);
            }
            finish();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        FullScreenHelper.setFullScreenOnWindowFocusChanged(this, hasFocus);
    }

    @Override
    public void onSurfaceCreated(SampleRender render) {
        try {
            planeRenderer = new PlaneRenderer(render);
            backgroundRenderer = new BackgroundRenderer(render);
            virtualSceneFramebuffer = new Framebuffer(render, /* width= */ 1, /* height= */ 1);

            // 선 렌더링을 위한 셰이더 초기화
            lineShader = Shader.createFromAssets(
                    render,
                    "shaders/line.vert",
                    "shaders/line.frag",
                    null)
                    .setVec4("u_Color", lineColor);

            // 선을 위한 버텍스 버퍼 초기화
            lineVertexBuffer = new VertexBuffer(render, 3, null);
            final VertexBuffer[] lineVertexBuffers = {lineVertexBuffer};
            lineMesh = new Mesh(render, Mesh.PrimitiveMode.LINE_STRIP, null, lineVertexBuffers);

        } catch (IOException e) {
            Log.e(TAG, "Failed to read a required asset file", e);
            messageSnackbarHelper.showError(this, "Failed to read a required asset file: " + e);
        }
    }

    @Override
    public void onSurfaceChanged(SampleRender render, int width, int height) {
        displayRotationHelper.onSurfaceChanged(width, height);
        virtualSceneFramebuffer.resize(width, height);
    }

    @Override
    public void onDrawFrame(SampleRender render) {
        if (session == null) {
            return;
        }

        if (!hasSetTextureNames) {
            session.setCameraTextureNames(
                    new int[] {backgroundRenderer.getCameraColorTexture().getTextureId()});
            hasSetTextureNames = true;
        }

        displayRotationHelper.updateSessionIfNeeded(session);

        Frame frame;
        try {
            frame = session.update();
        } catch (CameraNotAvailableException e) {
            Log.e(TAG, "Camera not available during onDrawFrame", e);
            messageSnackbarHelper.showError(this, "Camera not available. Try restarting the app.");
            return;
        }
        Camera camera = frame.getCamera();

        try {
            backgroundRenderer.setUseDepthVisualization(
                    render, depthSettings.depthColorVisualizationEnabled());
            backgroundRenderer.setUseOcclusion(render, depthSettings.useDepthForOcclusion());
        } catch (IOException e) {
            Log.e(TAG, "Failed to read a required asset file", e);
            messageSnackbarHelper.showError(this, "Failed to read a required asset file: " + e);
            return;
        }

        backgroundRenderer.updateDisplayGeometry(frame);

        if (camera.getTrackingState() == TrackingState.TRACKING
                && (depthSettings.useDepthForOcclusion()
                || depthSettings.depthColorVisualizationEnabled())) {
            try (Image depthImage = frame.acquireDepthImage16Bits()) {
                backgroundRenderer.updateCameraDepthTexture(depthImage);
            } catch (NotYetAvailableException e) {
                // This normally means that depth data is not available yet.
            }
        }

        handleTap(frame, camera);

        trackingStateHelper.updateKeepScreenOnFlag(camera.getTrackingState());

        String message = null;
        if (camera.getTrackingState() == TrackingState.PAUSED) {
            if (camera.getTrackingFailureReason() == TrackingFailureReason.NONE) {
                message = SEARCHING_PLANE_MESSAGE;
            } else {
                message = TrackingStateHelper.getTrackingFailureReasonString(camera);
            }
        } else if (hasTrackingPlane()) {
            if (wrappedAnchors.isEmpty()) {
                message = WAITING_FOR_TAP_MESSAGE;
            }
        } else {
            message = SEARCHING_PLANE_MESSAGE;
        }
        if (message == null) {
            messageSnackbarHelper.hide(this);
        } else {
            messageSnackbarHelper.showMessage(this, message);
        }

        if (frame.getTimestamp() != 0) {
            backgroundRenderer.drawBackground(render);
        }

        if (camera.getTrackingState() == TrackingState.PAUSED) {
            return;
        }

        camera.getProjectionMatrix(projectionMatrix, 0, Z_NEAR, Z_FAR);
        camera.getViewMatrix(viewMatrix, 0);

        planeRenderer.drawPlanes(
                render,
                session.getAllTrackables(Plane.class),
                camera.getDisplayOrientedPose(),
                projectionMatrix);

        // 앵커 사이에 선 그리기
        if (wrappedAnchors.size() >= 2) {
            float[] startPoint1 = new float[3];
            float[] endPoint1 = new float[3];
            
            // 첫 번째 선 (앵커 1-2)
            wrappedAnchors.get(0).getAnchor().getPose().getTranslation(startPoint1, 0);
            wrappedAnchors.get(1).getAnchor().getPose().getTranslation(endPoint1, 0);
            
            // 첫 번째 거리 계산
            float dx1 = endPoint1[0] - startPoint1[0];
            float dy1 = endPoint1[1] - startPoint1[1];
            float dz1 = endPoint1[2] - startPoint1[2];
            float frameDistance1 = (float) Math.sqrt(dx1 * dx1 + dy1 * dy1 + dz1 * dz1);
            
            // 첫 번째 거리 히스토리에 저장
            distanceHistory1[historyIndex] = frameDistance1;
            
            // 첫 번째 선 그리기
            float[] lineVertices1 = new float[] {
                startPoint1[0], startPoint1[1], startPoint1[2],
                endPoint1[0], endPoint1[1], endPoint1[2]
            };
            
            ByteBuffer bb1 = ByteBuffer.allocateDirect(lineVertices1.length * 4);
            bb1.order(ByteOrder.nativeOrder());
            FloatBuffer vertexBuffer1 = bb1.asFloatBuffer();
            vertexBuffer1.put(lineVertices1);
            vertexBuffer1.position(0);
            
            lineVertexBuffer.set(vertexBuffer1);
            
            Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
            lineShader.setMat4("u_ModelViewProjection", modelViewProjectionMatrix);
            
            GLES30.glLineWidth(lineWidth * 100);
            render.draw(lineMesh, lineShader);
            
            // 앵커가 3개일 때 두 번째 선 그리기
            if (wrappedAnchors.size() == 3) {
                float[] startPoint2 = new float[3];
                float[] endPoint2 = new float[3];
                
                // 두 번째 선 (앵커 2-3)
                wrappedAnchors.get(1).getAnchor().getPose().getTranslation(startPoint2, 0);
                wrappedAnchors.get(2).getAnchor().getPose().getTranslation(endPoint2, 0);
                
                // 두 번째 거리 계산
                float dx2 = endPoint2[0] - startPoint2[0];
                float dy2 = endPoint2[1] - startPoint2[1];
                float dz2 = endPoint2[2] - startPoint2[2];
                float frameDistance2 = (float) Math.sqrt(dx2 * dx2 + dy2 * dy2 + dz2 * dz2);
                
                // 두 번째 거리 히스토리에 저장
                distanceHistory2[historyIndex] = frameDistance2;
                
                // 두 번째 선 그리기
                float[] lineVertices2 = new float[] {
                    startPoint2[0], startPoint2[1], startPoint2[2],
                    endPoint2[0], endPoint2[1], endPoint2[2]
                };
                
                ByteBuffer bb2 = ByteBuffer.allocateDirect(lineVertices2.length * 4);
                bb2.order(ByteOrder.nativeOrder());
                FloatBuffer vertexBuffer2 = bb2.asFloatBuffer();
                vertexBuffer2.put(lineVertices2);
                vertexBuffer2.position(0);
                
                lineVertexBuffer.set(vertexBuffer2);
                render.draw(lineMesh, lineShader);
            }
            
            GLES30.glLineWidth(1.0f);
            
            // 히스토리 인덱스 업데이트
            historyIndex = (historyIndex + 1) % AVERAGE_FRAMES;
            if (historyIndex == 0) {
                isHistoryFull = true;
            }
            
            // 평균 거리 계산
            float sum1 = 0;
            float sum2 = 0;
            int count = isHistoryFull ? AVERAGE_FRAMES : historyIndex;
            
            for (int i = 0; i < count; i++) {
                sum1 += distanceHistory1[i];
                if (wrappedAnchors.size() == 3) {
                    sum2 += distanceHistory2[i];
                }
            }
            
            currentDistance1 = sum1 / count;
            if (wrappedAnchors.size() == 3) {
                currentDistance2 = sum2 / count;
            }
            
            // UI 스레드에서 거리 표시 업데이트
            runOnUiThread(() -> {
                String distanceText;
                distanceText = String.format(Locale.getDefault(),
                "%s\n"+
                "앵커 1-2 거리: %.1f cm\n" +
                "앵커 2-3 거리: %.1f cm",
                selectedItem,
                currentDistance1 * 100.0f,
                currentDistance2 * 100.0f);
                distanceTextView.setText(distanceText);
            });
        } else {
            // 앵커가 2개 미만일 때 거리 표시 초기화
            runOnUiThread(() -> distanceTextView.setText(""));
            // 히스토리 초기화
            historyIndex = 0;
            isHistoryFull = false;
        }
    }

    // Handle only one tap per frame, as taps are usually low frequency compared to frame rate.
    private void handleTap(Frame frame, Camera camera) {
        MotionEvent tap = tapHelper.poll();
        if (tap != null && camera.getTrackingState() == TrackingState.TRACKING) {
            // 화면의 중앙 좌표 계산
            float centerX = surfaceView.getWidth() / 2.0f;
            float centerY = surfaceView.getHeight() / 2.0f;

            List<HitResult> hitResultList;
            if (instantPlacementSettings.isInstantPlacementEnabled()) {
                hitResultList = frame.hitTestInstantPlacement(centerX, centerY, APPROXIMATE_DISTANCE_METERS);
            } else {
                hitResultList = frame.hitTest(centerX, centerY);
            }

            for (HitResult hit : hitResultList) {
                Trackable trackable = hit.getTrackable();
                if ((trackable instanceof Plane
                        && ((Plane) trackable).isPoseInPolygon(hit.getHitPose())
                        && (PlaneRenderer.calculateDistanceToPlane(hit.getHitPose(), camera.getPose()) > 0))
                        || (trackable instanceof Point
                        && ((Point) trackable).getOrientationMode()
                        == OrientationMode.ESTIMATED_SURFACE_NORMAL)
                        || (trackable instanceof InstantPlacementPoint)
                        || (trackable instanceof DepthPoint)) {
                    if (wrappedAnchors.size() >= 3) {
                        wrappedAnchors.get(0).getAnchor().detach();
                        wrappedAnchors.remove(0);
                    }

                    wrappedAnchors.add(new WrappedAnchor(hit.createAnchor(), trackable));
                    this.runOnUiThread(this::showOcclusionDialogIfNeeded);
                    break;
                }
            }
        }
    }

    /**
     * Shows a pop-up dialog on the first call, determining whether the user wants to enable
     * depth-based occlusion. The result of this dialog can be retrieved with useDepthForOcclusion().
     */
    private void showOcclusionDialogIfNeeded() {
        boolean isDepthSupported = session.isDepthModeSupported(Config.DepthMode.AUTOMATIC);
        if (!depthSettings.shouldShowDepthEnableDialog() || !isDepthSupported) {
            return; // Don't need to show dialog.
        }

        // Asks the user whether they want to use depth-based occlusion.
        new AlertDialog.Builder(this)
                .setTitle(R.string.options_title_with_depth)
                .setMessage(R.string.depth_use_explanation)
                .setPositiveButton(
                        R.string.button_text_enable_depth,
                        (DialogInterface dialog, int which) -> {
                            depthSettings.setUseDepthForOcclusion(true);
                        })
                .setNegativeButton(
                        R.string.button_text_disable_depth,
                        (DialogInterface dialog, int which) -> {
                            depthSettings.setUseDepthForOcclusion(false);
                        })
                .show();
    }
    
    /** Checks if we detected at least one plane. */
    private boolean hasTrackingPlane() {
        for (Plane plane : session.getAllTrackables(Plane.class)) {
            if (plane.getTrackingState() == TrackingState.TRACKING) {
                return true;
            }
        }
        return false;
    }

    /** Update state based on the current frame's light estimation. */
    private void updateLightEstimation(LightEstimate lightEstimate, float[] viewMatrix) {
        if (lightEstimate.getState() != LightEstimate.State.VALID) {
            virtualObjectShader.setBool("u_LightEstimateIsValid", false);
            return;
        }
        virtualObjectShader.setBool("u_LightEstimateIsValid", true);

        Matrix.invertM(viewInverseMatrix, 0, viewMatrix, 0);
        virtualObjectShader.setMat4("u_ViewInverse", viewInverseMatrix);

        updateMainLight(
                lightEstimate.getEnvironmentalHdrMainLightDirection(),
                lightEstimate.getEnvironmentalHdrMainLightIntensity(),
                viewMatrix);
        updateSphericalHarmonicsCoefficients(
                lightEstimate.getEnvironmentalHdrAmbientSphericalHarmonics());
        cubemapFilter.update(lightEstimate.acquireEnvironmentalHdrCubeMap());
    }

    private void updateMainLight(float[] direction, float[] intensity, float[] viewMatrix) {
        // We need the direction in a vec4 with 0.0 as the final component to transform it to view space
        worldLightDirection[0] = direction[0];
        worldLightDirection[1] = direction[1];
        worldLightDirection[2] = direction[2];
        Matrix.multiplyMV(viewLightDirection, 0, viewMatrix, 0, worldLightDirection, 0);
        virtualObjectShader.setVec4("u_ViewLightDirection", viewLightDirection);
        virtualObjectShader.setVec3("u_LightIntensity", intensity);
    }

    private void updateSphericalHarmonicsCoefficients(float[] coefficients) {
        // Pre-multiply the spherical harmonics coefficients before passing them to the shader. The
        // constants in sphericalHarmonicFactors were derived from three terms:
        //
        // 1. The normalized spherical harmonics basis functions (y_lm)
        //
        // 2. The lambertian diffuse BRDF factor (1/pi)
        //
        // 3. A <cos> convolution. This is done to so that the resulting function outputs the irradiance
        // of all incoming light over a hemisphere for a given surface normal, which is what the shader
        // (environmental_hdr.frag) expects.
        //
        // You can read more details about the math here:
        // https://google.github.io/filament/Filament.html#annex/sphericalharmonics

        if (coefficients.length != 9 * 3) {
            throw new IllegalArgumentException(
                    "The given coefficients array must be of length 27 (3 components per 9 coefficients");
        }

        // Apply each factor to every component of each coefficient
        for (int i = 0; i < 9 * 3; ++i) {
            sphericalHarmonicsCoefficients[i] = coefficients[i] * sphericalHarmonicFactors[i / 3];
        }
        virtualObjectShader.setVec3Array(
                "u_SphericalHarmonicsCoefficients", sphericalHarmonicsCoefficients);
    }

    /** Configures the session with feature settings. */
    private void configureSession() {
        Config config = session.getConfig();
        config.setLightEstimationMode(Config.LightEstimationMode.ENVIRONMENTAL_HDR);
        
        // 깊이 모드 강제 활성화
        config.setDepthMode(Config.DepthMode.AUTOMATIC);
        
        // 깊이 설정 활성화
        depthSettings.setUseDepthForOcclusion(true);
        
        if (instantPlacementSettings.isInstantPlacementEnabled()) {
            config.setInstantPlacementMode(InstantPlacementMode.LOCAL_Y_UP);
        } else {
            config.setInstantPlacementMode(InstantPlacementMode.DISABLED);
        }
        session.configure(config);
    }
}

/**
 * Associates an Anchor with the trackable it was attached to. This is used to be able to check
 * whether or not an Anchor originally was attached to an {@link InstantPlacementPoint}.
 */
class WrappedAnchor {
    private Anchor anchor;
    private Trackable trackable;

    public WrappedAnchor(Anchor anchor, Trackable trackable) {
        this.anchor = anchor;
        this.trackable = trackable;
    }

    public Anchor getAnchor() {
        return anchor;
    }

    public Trackable getTrackable() {
        return trackable;
    }
}