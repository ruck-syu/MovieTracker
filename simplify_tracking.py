with open('app/src/main/res/layout/activity_detail.xml', 'r') as f:
    content = f.read()

start = content.find('            <androidx.cardview.widget.CardView\n                android:layout_width="match_parent"\n                android:layout_height="wrap_content"\n                android:layout_marginStart="16dp"\n                android:layout_marginTop="16dp"\n                android:layout_marginEnd="16dp"\n                app:cardCornerRadius="28dp"\n                app:cardElevation="0dp">')
end = content.find('            </androidx.cardview.widget.CardView>', start) + len('            </androidx.cardview.widget.CardView>')

replacement = """            <TextView
                android:id="@+id/tvAddToList"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginStart="16dp"
                android:layout_marginTop="16dp"
                android:layout_marginEnd="16dp"
                android:background="@drawable/bg_button_primary"
                android:gravity="center"
                android:padding="14dp"
                android:text="@string/detail_add_to_my_list"
                android:textColor="@color/white"
                android:textStyle="bold" />"""

# Note: The first match of the CardView in the file after reordering is the Tracking card view?
# Wait, let's check the order again.
# The layout after reordering:
# 1. Poster CardView
# 2. LinearLayout (Title, Year, Type)
# 3. Tracking CardView
# 4. Details CardView (starts with <LinearLayout padding="18dp" and <TextView text="IMDb")

# Let's find the tracking CardView uniquely by searching for its content (e.g., tvTrackingStatus).
tracking_start = content.rfind('            <androidx.cardview.widget.CardView', 0, content.find('tvTrackingStatus'))
tracking_end = content.find('            </androidx.cardview.widget.CardView>', tracking_start) + len('            </androidx.cardview.widget.CardView>')

if tracking_start != -1 and tracking_end > tracking_start:
    new_content = content[:tracking_start] + replacement + content[tracking_end:]
    with open('app/src/main/res/layout/activity_detail.xml', 'w') as f:
        f.write(new_content)
    print("Replaced tracking section")
else:
    print("Could not find tracking section")

