package com.example.fresh_picks;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;

/**
 * A simple {@link Fragment} subclass.
 */
public class Home extends Fragment {

    public Home() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Setting up CardView click listeners
        setupCardViewClickListeners(view);

        return view;
    }

    private void setupCardViewClickListeners(View view) {
        int[] cardViewIds = {
                R.id.cardView1, R.id.cardView2, R.id.cardView3,
                R.id.cardView4, R.id.cardView5, R.id.cardView6,
                R.id.cardView7, R.id.cardView8, R.id.cardView9,
                R.id.cardView10, R.id.cardView11, R.id.cardView12
        };

        String[] titles = getResources().getStringArray(R.array.card_titles);

        for (int i = 0; i < cardViewIds.length; i++) {
            CardView cardView = view.findViewById(cardViewIds[i]);
            String title = titles[i];
            cardView.setOnClickListener(v -> {
                Log.d("Home", "Navigating to ListsView with category: " + title);
                navigateToListsView(title);
            });
        }
    }

    private void navigateToListsView(String title) {
        ListsView listsViewFragment = ListsView.newInstance(title);

        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, listsViewFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
