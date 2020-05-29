package com.integrals.inlens.Helper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.integrals.inlens.Models.DirectoryModel;
import com.integrals.inlens.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@SuppressLint("ValidFragment")
public class DirectoryFragment extends BottomSheetDialogFragment {

    public interface ResumeCallback
    {
        void reloadData();
    }

    Context context;
    List<DirectoryModel>dirPaths;
    SharedPreferences dirPreference;
    SharedPreferences.Editor dirEditor;
    ResumeCallback resumeCallback;

    public DirectoryFragment(Context context) {
        this.context = context;
        dirPaths =new ArrayList<>();
        dirPreference = context.getSharedPreferences(AppConstants.CURRENT_COMMUNITY_PREF,Context.MODE_PRIVATE);
        dirEditor = dirPreference.edit();
        resumeCallback = (ResumeCallback) context;

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view;

        SharedPreferences themePref = context.getSharedPreferences(AppConstants.appDataPref, Context.MODE_PRIVATE);
        if(themePref.contains(AppConstants.appDataPref_theme))
        {
            if(themePref.getString(AppConstants.appDataPref_theme,AppConstants.themeLight).equals(AppConstants.themeLight))
            {
                ContextWrapper contextWrapper = new ContextWrapper(context);
                contextWrapper.setTheme(R.style.AppTheme);
                view = inflater.cloneInContext(contextWrapper).inflate(R.layout.fragment_directory, container, false);

            }
            else
            {
                ContextWrapper contextWrapper = new ContextWrapper(context);
                contextWrapper.setTheme(R.style.DarkTheme);
                view = inflater.cloneInContext(contextWrapper).inflate(R.layout.fragment_directory, container, false);

            }
        }
        else
        {
            ContextWrapper contextWrapper = new ContextWrapper(context);
            contextWrapper.setTheme(R.style.AppTheme);
            view = inflater.cloneInContext(contextWrapper).inflate(R.layout.fragment_directory, container, false);

        }

        RecyclerView dirRecyclerview = view.findViewById(R.id.dirRecyclerview);
        dirRecyclerview.setHasFixedSize(true);
        dirRecyclerview.setLayoutManager(new LinearLayoutManager(context));
        DirectoryAdapter directoryAdapter = new DirectoryAdapter(context);
        dirRecyclerview.setAdapter(directoryAdapter);

        Uri uri;
        Cursor cursor;
        int column_index_data;
        String absolutePathOfImage;
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.MediaColumns.DATA,MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
        cursor = context.getContentResolver().query(uri, projection, null, null, null);

        String directories = dirPreference.getString(AppConstants.SELECTED_DIRECTORIES,"");

        try
        {
            cursor.moveToLast();
            column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);

            // currently detect all images we need to modify it to detect only images in camera

            do
            {
                absolutePathOfImage = cursor.getString(column_index_data);

                String[] pathSegments = absolutePathOfImage.split("/");

                if(pathSegments.length>4 && !getPaths(dirPaths).contains(pathSegments[4]))
                {
                    if(directories.contains(pathSegments[4].toLowerCase()))
                    {
                        dirPaths.add(new DirectoryModel(pathSegments[4],true));
                        directoryAdapter.notifyDataSetChanged();
                    }
                    else
                    {
                        dirPaths.add(new DirectoryModel(pathSegments[4],false));
                        directoryAdapter.notifyDataSetChanged();

                    }

                }

            }while (cursor.moveToPrevious());


        }
        catch (Exception e)
        {
            //todo There are zero photos in the phone.
            //Log.i(AppConstants.PHOTO_SCAN_WORK,"Exception caught"+ e.toString());

        }

        Button dirSaveButton = view.findViewById(R.id.dirSaveButton);
        dirSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String directoryPath="";
                for(int i=0;i<dirPaths.size();i++)
                {
                    if(dirPaths.get(i).isSelected())
                    {
                        directoryPath=directoryPath+" "+dirPaths.get(i).getDirPath().toLowerCase();
                    }
                }

                dirEditor.putString(AppConstants.SELECTED_DIRECTORIES,directoryPath);
                dirEditor.commit();
                resumeCallback.reloadData();

            }
        });


        return view;
    }


    private List<String> getPaths(List<DirectoryModel> dirPaths) {
        List<String> paths =new ArrayList<>();
        for(int i=0;i<dirPaths.size();i++)
        {
            paths.add(dirPaths.get(i).getDirPath());
        }

        return paths;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getDialog().setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                BottomSheetDialog d = (BottomSheetDialog) dialog;
                CoordinatorLayout coordinatorLayout = (CoordinatorLayout) d.findViewById(R.id.directory_bottomsheet_wrapper);
                View bottomSheetInternal = d.findViewById(R.id.directory_bottomsheet);
                BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetInternal);
                //bottomSheetBehavior.setHidable(false);
                BottomSheetBehavior.from((View)coordinatorLayout.getParent()).setPeekHeight(bottomSheetInternal.getHeight());
                bottomSheetBehavior.setPeekHeight(bottomSheetInternal.getHeight());
                coordinatorLayout.getParent().requestLayout();

            }
        });
    }

    public class DirectoryAdapter extends RecyclerView.Adapter<DirectoryAdapter.DirectoryViewHolder>
    {
        Context context;

        public DirectoryAdapter(Context context) {
            this.context = context;
        }


        @NonNull
        @Override
        public DirectoryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new DirectoryViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_directory_single_item,viewGroup,false));
        }

        @Override
        public void onBindViewHolder(@NonNull DirectoryViewHolder directoryViewHolder, int i) {

            directoryViewHolder.dirNameTextView.setText(dirPaths.get(i).getDirPath());
            if(dirPaths.get(i).isSelected())
            {
                directoryViewHolder.dirSelectionSwitch.setChecked(true);
            }
            else
            {
                directoryViewHolder.dirSelectionSwitch.setChecked(false);
            }

            directoryViewHolder.dirSelectionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                    if(b)
                    {
                        directoryViewHolder.dirSelectionSwitch.setChecked(true);
                        dirPaths.get(i).setSelected(true);
                    }
                    else
                    {
                        directoryViewHolder.dirSelectionSwitch.setChecked(false);
                        dirPaths.get(i).setSelected(false);
                    }

                }
            });

        }


        @Override
        public int getItemCount() {
            return dirPaths.size();
        }

        public class DirectoryViewHolder extends RecyclerView.ViewHolder {

            TextView dirNameTextView;
            Switch dirSelectionSwitch;

            public DirectoryViewHolder(@NonNull View itemView) {
                super(itemView);

                dirSelectionSwitch = itemView.findViewById(R.id.dirSelectionSwitch);
                dirNameTextView = itemView.findViewById(R.id.dirNameTextView);
            }
        }
    }
}

