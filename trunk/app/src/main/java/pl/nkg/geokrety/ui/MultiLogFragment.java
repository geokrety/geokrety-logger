package pl.nkg.geokrety.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import pl.nkg.geokrety.GeoKretyApplication;
import pl.nkg.geokrety.R;

public class MultiLogFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private Unbinder unbinder;

    @BindView(R.id.gkListView) ListView gkListView;
    @BindView(R.id.logListView) ListView logListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_multilog, container, false);
        unbinder = ButterKnife.bind(this, view);

        gkListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        logListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        GeoKretyApplication application = (GeoKretyApplication) getContext().getApplicationContext();

        gkListView.setAdapter(new InventoryListAdapter(getContext(), application.getStateHolder().getInventoryDataSource().loadInventory()));
        logListView.setAdapter(new GCLogListAdapter(getContext(), application.getStateHolder().getGeocacheLogDataSource().loadLastLogs()));

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

    public static MultiLogFragment newInstance() {
        return new MultiLogFragment();
    }

    public interface OnFragmentInteractionListener {
    }
}
