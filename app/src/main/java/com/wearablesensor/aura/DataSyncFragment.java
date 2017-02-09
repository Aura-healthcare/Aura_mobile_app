package com.wearablesensor.aura;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wearablesensor.aura.data.DateIso8601Mapper;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DataSyncFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DataSyncFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DataSyncFragment extends Fragment {
    private final String TAG = this.getClass().getSimpleName();

    @BindView(R.id.data_sync_progress_bar) ProgressBar mProgressBar;
    @BindView(R.id.data_sync_image_view) ImageView mImageView;
    @BindView(R.id.data_sync_last_sync) TextView mLastSyncView;
    private OnFragmentInteractionListener mListener;

    public DataSyncFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters
     *
     * @return A new instance of fragment DevicePairingFragment.
     */

    public static DataSyncFragment newInstance() {
        DataSyncFragment fragment = new DataSyncFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_data_sync, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onDataSyncFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public void displayStartPushData() {
        mProgressBar.setVisibility(View.VISIBLE);
        mImageView.setVisibility(View.GONE);
    }

    public void displayProgressPushData(Integer iProgress) {
        mProgressBar.setProgress(iProgress);
    }

    public void displaySuccessPushData(Date iCurrentSync) {
        mProgressBar.setVisibility(View.GONE);
        mProgressBar.setProgress(0);

        mImageView.setVisibility(View.VISIBLE);

        updateLastSyncDisplay(iCurrentSync);
    }

    public void displayFailPushData(){
        mProgressBar.setVisibility(View.GONE);
        mProgressBar.setProgress(0);

        mImageView.setVisibility(View.VISIBLE);
    }

    public void updateLastSyncDisplay(Date iLastSync){
        mLastSyncView.setText(getString(R.string.last_sync) +  DateIso8601Mapper.getString(iLastSync));
    }



    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onDataSyncFragmentInteraction(Uri uri);
    }
}
