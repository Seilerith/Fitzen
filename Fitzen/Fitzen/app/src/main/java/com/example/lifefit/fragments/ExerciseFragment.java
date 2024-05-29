package com.example.lifefit.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.lifefit.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class ExerciseFragment extends Fragment {

    // Declare variables to hold selected chip ids
    private int selectedBodyChipId = View.NO_ID;
    private int selectedEquipmentChipId = View.NO_ID;
    private int selectedGenderId = View.NO_ID;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exercise, container, false);

        // Add chips to the body selection ChipGroup
        ChipGroup bodySelectionChipGroup = view.findViewById(R.id.bodySelectionChipGroup);
        String[] bodyParts = {"Üst Sırt", "Orta Sırt", "Yan Sırt", "Alt Sırt", "Ön Omuz", "Arka Omuz", "Ön Kol", "Arka Kol", "Bilek", "Gögüs", "Yan Karın", "Karın", "Ön Bacak", "Arka Bacak", "Kalça", "Baldır"};
        addChipsToChipGroup(bodySelectionChipGroup, bodyParts);

        // Add chips to the equipment ChipGroup
        ChipGroup equipmentChipGroup = view.findViewById(R.id.equipmentChipGroup);
        String[] equipment = {"Ekipmansız", "Halter", "Dambıllar", "Vücut Ağırlığı", "Makine", "Medicine Ball", "Kettlebeller", "Esnetme Egzersizleri", "Kablolar", "Bant", "Tabak", "Trx", "Yoga", "Bosu-Ball", "Kettlebeller", "Bitruvian", "Kardiyovasküler", "Smith-Makinesi"};
        addChipsToChipGroup(equipmentChipGroup, equipment);

        // Find gender switch
        SwitchMaterial genderSwitch = view.findViewById(R.id.genderSwitch);

        // Set listener for gender switch
        genderSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Erkek seçildiğinde yapılacak işlemler
                    selectedGenderId = R.id.genderSwitch; // Erkek seçildiğinde id'sini kaydet
                } else {
                    // Kadın seçildiğinde yapılacak işlemler
                    selectedGenderId = View.NO_ID; // Kadın seçildiğinde id'sini sıfırla
                }
            }
        });

        return view;
    }

    private void addChipsToChipGroup(ChipGroup chipGroup, String[] items) {
        for (String item : items) {
            Chip chip = new Chip(requireContext(), null, com.google.android.material.R.style.Widget_Material3_Chip_Filter); // Set the style here
            chip.setText(item);
            chip.setCheckable(true);
            chip.setClickable(true);

            // Set an OnCheckedChangeListener for each chip
            chip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    // If chip is checked, update selected chip id
                    if (isChecked) {
                        // Clear selection for other chips in the same group
                        clearChipGroupSelection(chipGroup);
                        // Update selected chip id
                        if (chipGroup.getId() == R.id.bodySelectionChipGroup) {
                            selectedBodyChipId = chip.getId();
                        } else if (chipGroup.getId() == R.id.equipmentChipGroup) {
                            selectedEquipmentChipId = chip.getId();
                        }
                    }
                }
            });

            chipGroup.addView(chip);
        }
    }

    // Method to clear selection for other chips in the same group
    private void clearChipGroupSelection(ChipGroup chipGroup) {
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            if (chip.isChecked() && chip.getId() != selectedBodyChipId && chip.getId() != selectedEquipmentChipId) {
                chip.setChecked(false);
            }
        }
    }

    // Method to get the selected body chip id
    public int getSelectedBodyChipId() {
        return selectedBodyChipId;
    }

    // Method to get the selected equipment chip id
    public int getSelectedEquipmentChipId() {
        return selectedEquipmentChipId;
    }

    // Method to get the selected gender switch id
    public int getSelectedGenderId() {
        return selectedGenderId;
    }
}
