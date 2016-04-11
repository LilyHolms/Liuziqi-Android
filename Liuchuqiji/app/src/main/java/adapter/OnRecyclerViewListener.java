package adapter;

/**
 * Created by Lily on 16/4/6.
 */
public interface OnRecyclerViewListener {
    void onItemClick(int position);
    boolean onItemLongClick(int position);
}