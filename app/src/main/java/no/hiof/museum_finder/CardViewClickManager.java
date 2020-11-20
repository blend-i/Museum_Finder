package no.hiof.museum_finder;

import android.widget.ToggleButton;

public interface CardViewClickManager {
    void onCardViewClick(int position);
    void onCardViewToggleButtonCheckedChanged(int position, ToggleButton favourite, boolean isChecked);
}
