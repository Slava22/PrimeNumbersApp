package andrushkva.com.primenumbers;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vyacheslav on 21.07.2018.
 */

public class PrimeNumbersAdapter extends RecyclerView.Adapter<PrimeNumbersAdapter.PrimeNumbersViewHolder> {

    private List<Integer> primeNumbers;

    public PrimeNumbersAdapter(Context mContext) {
        this.primeNumbers = new ArrayList<>();
    }

    public void changeList(List<Integer> list) {
        primeNumbers.clear();
        primeNumbers.addAll(list);
    }

    public void addNumbers(List<Integer> list) {
        primeNumbers.addAll(list);
    }

    public void clearNumbers() {
        primeNumbers.clear();
    }

    @Override
    public long getItemId(int position) {
        return primeNumbers.get(position).hashCode();
    }

    @Override
    public int getItemCount() {
        return primeNumbers.size();
    }

    @Override
    public PrimeNumbersViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview, viewGroup, false);
        PrimeNumbersViewHolder primeNumbersViewHolder = new PrimeNumbersViewHolder(v);
        return primeNumbersViewHolder;
    }

    @Override
    public void onBindViewHolder(PrimeNumbersViewHolder primeNumbersViewHolder, int i) {
        primeNumbersViewHolder.tvNumber.setText(String.valueOf(primeNumbers.get(i)));
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public class PrimeNumbersViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumber;

        PrimeNumbersViewHolder(final View itemView) {
            super(itemView);
            tvNumber = itemView.findViewById(R.id.tv_number);
        }
    }
}
