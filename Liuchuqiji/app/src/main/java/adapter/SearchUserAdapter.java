package adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import entity.User;

/**
 * Created by Lily on 16/4/6.
 */
public class SearchUserAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
/**
 app/build.gradle中添加compile 'com.android.support:recyclerview-v7:23.1.1'才能使用RecyclerView
*/
    private List<User> users = new ArrayList<>();

    public SearchUserAdapter() {}

    public void setDatas(List<User> list) {
        users.clear();
        if (null != list) {
            users.addAll(list);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SearchUserHolder(parent.getContext(), parent);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((BaseViewHolder)holder).bindData(users.get(position));
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    @Override
    public int getItemCount() {
        return users.size();
    }
}
