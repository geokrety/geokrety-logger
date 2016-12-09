package pl.nkg.geokrety.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import pl.nkg.geokrety.GeoKretyApplication;
import pl.nkg.geokrety.R;
import pl.nkg.geokrety.activities.GeoKretLogsActivity;
import pl.nkg.geokrety.data.GeoKret;
import pl.nkg.geokrety.data.GeocacheLog;
import pl.nkg.geokrety.data.StateHolder;

public class MultiLogFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private Unbinder unbinder;

    @BindView(R.id.gkListView) ListView gkListView;
    @BindView(R.id.logListView) ListView logListView;

    private InventoryListAdapter mInventoryListAdapter;
    private GCLogListAdapter mGCLogListAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GeoKretyApplication application = (GeoKretyApplication) getContext().getApplicationContext();
        StateHolder stateHolder = application.getStateHolder();
        mInventoryListAdapter = new InventoryListAdapter(getContext(), stateHolder.getInventoryDataSource().loadInventory());
        mGCLogListAdapter = new GCLogListAdapter(getContext(), application.getStateHolder().getGeocacheLogDataSource().loadLastLogs());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_multilog, container, false);
        unbinder = ButterKnife.bind(this, view);

        gkListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        logListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);


        gkListView.setAdapter(mInventoryListAdapter);
        logListView.setAdapter(mGCLogListAdapter);

        AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                fireSelectionListUpdated();
            }
        };

        gkListView.setOnItemClickListener(listener);
        logListView.setOnItemClickListener(listener);

        return view;
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
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void fireSelectionListUpdated() {
        if (mListener == null) {
            return;
        }

        List<GeoKret> geoKretList = getSelectedGeoKretList();
        List<GeocacheLog> geocacheLogList = getSelectedGeocacheLogList();

        mListener.onSelectionListUpdated(geoKretList, geocacheLogList);
    }

    public List<GeoKret> getSelectedGeoKretList() {
        List<GeoKret> geoKretList = new ArrayList<>();

        for (int i = 0; i < mInventoryListAdapter.getCount(); i++) {
            if (logListView.isItemChecked(i)) {
                geoKretList.add(mInventoryListAdapter.getItem(i));
            }
        }

        return geoKretList;
    }

    public List<GeocacheLog> getSelectedGeocacheLogList() {
        List<GeocacheLog> geocacheLogList = new ArrayList<>();

        for (int i = 0; i < mGCLogListAdapter.getCount(); i++) {
            if (gkListView.isItemChecked(i)) {
                geocacheLogList.add(mGCLogListAdapter.getItem(i));
            }
        }

        return geocacheLogList;
    }

    public static MultiLogFragment newInstance() {
        return new MultiLogFragment();
    }

    public interface OnFragmentInteractionListener {
        void onSelectionListUpdated(List<GeoKret> geoKretList, List<GeocacheLog> geocacheLogList);
    }
}
