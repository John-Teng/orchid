package com.tengo.orchid.View;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tengo.orchid.Model.DataChangedDelegate;
import com.tengo.orchid.Presenter.GridPhotosPresenter;
import com.tengo.orchid.R;
import com.tengo.orchid.View.Adapters.GridPhotoAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * Created by johnteng on 2018-03-05.
 */

public class PhotosTabFragment extends android.support.v4.app.Fragment
        implements GridPhotoAdapter.GridPhotosDelegate, DataChangedDelegate {

    public interface GridPhotosPresenterDelegate {
        String getThumbnail(int position);

        int getItemCount();

        int getRating(int position);

        void addPhoto(Uri uri);

        void addPhotos(List<Uri> uris);
    }

    private RecyclerView mRecyclerView;
    private GridPhotoAdapter mAdapter;
    private GridPhotosPresenterDelegate mPresenterDelegate;
    private final int REQUEST_IMAGE_GET = 0;
    private final int NUM_COLUMNS = 4;
    private final String REQUEST_TITLE = "Select Images";

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab_photos, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View view = getView();
        if (view == null) {
            return;
        }
        mPresenterDelegate = new GridPhotosPresenter(getContext(), this);
        mAdapter = new GridPhotoAdapter(this);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.photos_recyclerview);
        GridLayoutManager gm = new GridLayoutManager(getContext(), NUM_COLUMNS);
        mRecyclerView.setLayoutManager(gm);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void openSinglePhoto(int position) {
        Intent intent = new Intent(getContext(), SinglePhotoActivity.class);
        Bundle params = new Bundle();
        params.putInt(getString(R.string.param_photo_id), position);
        intent.putExtras(params);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case RESULT_OK: {
                if (requestCode == REQUEST_IMAGE_GET) {
                    if (data.getData() != null) {
                        getContext().getContentResolver().takePersistableUriPermission(data.getData(), Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        mPresenterDelegate.addPhoto(data.getData());
                    } else if (data.getClipData() != null) {
                        ClipData clipData = data.getClipData();
                        List<Uri> uriArray = new ArrayList<Uri>();
                        for (int i = 0; i < clipData.getItemCount(); ++i) {
                            ClipData.Item item = clipData.getItemAt(i);
                            Uri uri = item.getUri();
                            getContext().getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            uriArray.add(uri);
                        }
                        mPresenterDelegate.addPhotos(uriArray);
                    }
                }
                break;
            }
            case RESULT_CANCELED:
                // do something:
                break;
            default:
                //do something:
                break;
        }
    }

    @Override
    public Bitmap getThumbnail(int position) {
        String uriString = mPresenterDelegate.getThumbnail(position);
        Bitmap thumbnail = null;
        try {
            thumbnail = ThumbnailUtils.extractThumbnail(MediaStore.Images.Media.getBitmap(
                    getContext().getContentResolver(), Uri.parse(uriString)), 200, 200);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return thumbnail;
    }

    @Override
    public int getListSize() {
        return mPresenterDelegate.getItemCount();
    }

    @Override
    public int getRating(int position) {
        return mPresenterDelegate.getRating(position);
    }

    @Override
    public void onAddPhoto() {
        // TODO after inserting in db, created new view items and add them to adapter
        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, REQUEST_TITLE), REQUEST_IMAGE_GET);
    }

    @Override
    public void onSelectPhoto(int position) {
        openSinglePhoto(position);
    }

    @Override
    public void onDataChanged() {
        mAdapter.notifyDataSetChanged();
    }
}
