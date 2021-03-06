package com.pukhuriandbeels.limknowpilot;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.pukhuriandbeels.limknowpilot.model.Animal;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class LakeARQuizActivity extends AppCompatActivity {

    public static int questionCount = 0;

    public static ArrayList<Animal> animals = new ArrayList<>();

    private TextView questionTextView;
    private RadioGroup questionRadioGroup;
    private RadioButton[] radioButtons;
    private Button submit;
    private ProgressBar progressBar;
    private TextView textViewAnswer;

    private String answer;
    private String mScale;

    private FirebaseAuth firebaseAuth;
    private Button buttonCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lake_a_r_quiz);
        firebaseFirestoreSetup();
        initialize();
    }

    private void initialize() {
        answer = "";
        mScale = "";

        questionTextView = findViewById(R.id.animal_ar_quiz_question);
        questionRadioGroup = findViewById(R.id.animal_ar_radio_group);
        progressBar = findViewById(R.id.connection_status);

        radioButtons = new RadioButton[3];
        radioButtons[0] = findViewById(R.id.animal_radio_1);
        radioButtons[1] = findViewById(R.id.animal_radio_2);
        radioButtons[2] = findViewById(R.id.animal_radio_3);

        textViewAnswer = findViewById(R.id.answer);

        submit = findViewById(R.id.button_animal_ar);
        buttonCancel = findViewById(R.id.button_animal_ar_cancel);

        questionTextView.setVisibility(View.GONE);
        questionRadioGroup.setVisibility(View.GONE);

        radioButtons[0].setVisibility(View.GONE);
        radioButtons[1].setVisibility(View.GONE);
        radioButtons[2].setVisibility(View.GONE);
        textViewAnswer.setVisibility(View.GONE);

        submit.setText("Submit");
        submit.setVisibility(View.GONE);
        buttonCancel.setVisibility(View.GONE);

        progressBar.setVisibility(View.VISIBLE);

        if (questionCount == animals.size()) {
            questionCount = 0;
        }

        if (animals.size() > 0 && questionCount < animals.size()) {
            questionTextView.setText(animals.get(questionCount).getAssociatedQuestion());
            radioButtons[0].setText(animals.get(questionCount).getOptionOne());
            radioButtons[1].setText(animals.get(questionCount).getOptionTwo());
            radioButtons[2].setText(animals.get(questionCount).getOptionThree());

            questionTextView.setVisibility(View.VISIBLE);
            questionRadioGroup.setVisibility(View.VISIBLE);

            radioButtons[0].setVisibility(View.VISIBLE);
            radioButtons[1].setVisibility(View.VISIBLE);
            radioButtons[2].setVisibility(View.VISIBLE);

            submit.setVisibility(View.VISIBLE);
            buttonCancel.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);

        }

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (submit.getText().toString().equalsIgnoreCase("submit")) {
                    if (answer.equalsIgnoreCase(animals.get(questionCount).getAnimalCommonName())) {
                        textViewAnswer.setText("Congratulations! Your answer is correct.");
                        textViewAnswer.setTextColor(getResources().getColor(R.color.colorAccentGreen));
                        textViewAnswer.setVisibility(View.VISIBLE);
                    } else {
                        textViewAnswer.setText(String.format("No worries.The correct answer is: %s", animals.get(questionCount).getAnimalCommonName()));
                        textViewAnswer.setTextColor(getResources().getColor(R.color.colorKingFisherOrange));
                        textViewAnswer.setVisibility(View.VISIBLE);
                    }
                    submit.setText("Experience 3D " + animals.get(questionCount).getAnimalCommonName());
                } else if (submit.getText().toString().equalsIgnoreCase("Experience 3D " + animals.get(questionCount).getAnimalCommonName())) {
                    Intent intent = new Intent(LakeARQuizActivity.this, LakeARActivity.class);
                    startActivity(intent);
                }
            }
        });

        questionRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radioButton = findViewById(questionRadioGroup.getCheckedRadioButtonId());
                answer = radioButton.getText().toString();
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                questionCount = 0;
                animals.clear();
                Toast.makeText(getApplicationContext(), "See you soon!", Toast.LENGTH_SHORT).show();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                Intent intent = new Intent(LakeARQuizActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void firebaseFirestoreSetup() {
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        if (animals.size() <= 0) {
            CollectionReference collectionReference = firebaseFirestore.collection("AR Biodiversity");
            collectionReference.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                        Animal animal = new Animal();
                        if (documentSnapshot.contains("scale")) {
                            mScale = documentSnapshot.getString("scale");
                            animal.setAnimalScale(Float.valueOf(mScale));
                        }
                        animal.setAnimalCommonName(documentSnapshot.getString("common_name"));
                        animal.setAnimalName(documentSnapshot.getString("name"));
                        animal.setAnimalWaterbodyAssociation(documentSnapshot.getString("waterbody_association"));
                        animal.setAnimalImageURL(documentSnapshot.getString("image_url"));
                        animal.setAnimalThreat(documentSnapshot.getString("threats"));
                        animal.setAnimalImageCredits(documentSnapshot.getString("image_credits"));
                        animal.setAnimalARModelURL(documentSnapshot.getString("ar_model_url"));

                        animal.setAssociatedQuestion(documentSnapshot.getString("question"));

                        animal.setOptionOne(documentSnapshot.getString("option_one"));
                        animal.setOptionTwo(documentSnapshot.getString("option_two"));
                        animal.setOptionThree(documentSnapshot.getString("option_three"));

                        animals.add(animal);
                    }
                        questionTextView.setText(animals.get(questionCount).getAssociatedQuestion());
                        radioButtons[0].setText(animals.get(questionCount).getOptionOne());
                        radioButtons[1].setText(animals.get(questionCount).getOptionTwo());
                        radioButtons[2].setText(animals.get(questionCount).getOptionThree());

                        questionTextView.setVisibility(View.VISIBLE);
                        questionRadioGroup.setVisibility(View.VISIBLE);

                        radioButtons[0].setVisibility(View.VISIBLE);
                        radioButtons[1].setVisibility(View.VISIBLE);
                        radioButtons[2].setVisibility(View.VISIBLE);

                        submit.setVisibility(View.VISIBLE);
                        buttonCancel.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);

                }
            });

            collectionReference.get().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        startActivity(intent);
        finish();
    }
}