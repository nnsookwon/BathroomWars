package io.nnsookwon.bathroom_wars;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by nnsoo on 12/31/2016.
 */

public class FriendsListDialogFragment extends DialogFragment {

    RecyclerView recyclerView;
    MyRecyclerAdapter adapter;
    ArrayList<FacebookFriend> friendsList;
    DialogListener dialogListener;

    public interface DialogListener {
        void dialogCancelled();
    }

   /* @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.friend_list_recycler_view, container);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.friend_list_recycler_view);
        //recyclerView.setHasFixedSize(true);


        friendsList = new ArrayList<>();
        friendsList.add(new FacebookFriend("hello", "hello", "hello"));
        adapter = new MyRecyclerAdapter(friendsList);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        return rootView;
    }*/

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View rootView = getActivity().getLayoutInflater().inflate(R.layout.friend_list_recycler_view, null);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.friend_list_recycler_view);
        recyclerView.setHasFixedSize(true);

        friendsList = new ArrayList<>();
        friendsList.add(new FacebookFriend("hello", "hello", "hello"));
        adapter = new MyRecyclerAdapter(friendsList);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));

        return new AlertDialog.Builder(getActivity())
                .setTitle("Choose a friend to battle")
                .setView(rootView)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialogListener.dialogCancelled();
                    dialog.dismiss();
                }
                })
                .create();
    }

    public void setFriendsList(ArrayList<FacebookFriend> list) {
        friendsList = list;
        adapter = new MyRecyclerAdapter(friendsList);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        adapter.notifyDataSetChanged();

        Log.d("friends", adapter.facebookFriends.toString());
    }

    public void setDialogListener(DialogListener listener){
        dialogListener = listener;
    }
}
