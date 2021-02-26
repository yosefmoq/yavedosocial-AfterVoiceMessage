package ru.ifsoft.network.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import ru.ifsoft.network.R;

public class ActionSavePostFragment extends BottomSheetDialogFragment implements View.OnClickListener {
    public static ActionSavePostFragment newInstanse(){
        return new ActionSavePostFragment();
    }
    private ItemClickListener mListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_post,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.llsaveDraft).setOnClickListener(this);
        view.findViewById(R.id.llDiscard).setOnClickListener(this);
        view.findViewById(R.id.llContinue).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        mListener.onItemClick(v.getId());
        dismiss();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ItemClickListener) {
            mListener = (ItemClickListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ItemClickListener");
        }

    }

    public interface ItemClickListener {
        void onItemClick(int id);
    }

}
