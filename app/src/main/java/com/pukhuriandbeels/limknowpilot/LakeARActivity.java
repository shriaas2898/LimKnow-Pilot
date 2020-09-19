package com.pukhuriandbeels.limknowpilot;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.assets.RenderableSource;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.StorageReference;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class LakeARActivity extends AppCompatActivity {

    public static int count = 0;
    private ArFragment arFragment;
    private TextView textViewGuide;
    private ModelRenderable modelRenderable;
    private String MODEL_URL;
    private FirebaseAuth firebaseAuth;
    private StorageReference firebaseStorageReference;
    private float scale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lake_ar);
        initialize();
        setListeners();
    }

    private void initialize() {
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.lake_ar_fragment);
        textViewGuide = findViewById(R.id.lake_ar_guide);

        MODEL_URL = LakeARQuizActivity.animals.get(LakeARQuizActivity.questionCount).getAnimalARModelURL();
        scale = LakeARQuizActivity.animals.get(LakeARQuizActivity.questionCount).getAnimalScale();
        arFragment.getArSceneView().getPlaneRenderer().setVisible(true);
    }

    private void setListeners() {
        arFragment.setOnTapArPlaneListener(new BaseArFragment.OnTapArPlaneListener() {
            @Override
            public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
                if (plane.getType() != Plane.Type.HORIZONTAL_UPWARD_FACING)
                    return;
                placeModel(hitResult.createAnchor());
            }
        });
    }

    private void placeModel(Anchor anchor) {
        ModelRenderable.builder().setSource(arFragment.getContext(), RenderableSource.builder()
                .setSource(this, Uri.parse(MODEL_URL), RenderableSource.SourceType.GLB)
                .setScale(scale)
                .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                .build()

        ).setRegistryId(MODEL_URL)
                .build()
                .thenAccept(renderable -> {
                    Toast.makeText(LakeARActivity.this, "Reached here", Toast.LENGTH_SHORT).show();
                    addNodeToScene(renderable, anchor);
                }).exceptionally(throwable -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(throwable.getMessage()).show();
            return null;
        });
    }

    private void addNodeToScene(ModelRenderable renderable, Anchor anchor) {
        AnchorNode anchorNode = new AnchorNode(anchor);
        TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());
        node.setOnTapListener(new Node.OnTapListener() {
            @Override
            public void onTap(HitTestResult hitTestResult, MotionEvent motionEvent) {
                Intent intent = new Intent(LakeARActivity.this, ARAnimalItemActivity.class);
                startActivity(intent);
            }
        });
        node.setRenderable(renderable);
        node.setParent(anchorNode);
        arFragment.getArSceneView().getScene().addChild(anchorNode);
        node.select();
    }

}