with open('app/src/main/res/layout/activity_detail.xml', 'r') as f:
    content = f.read()

# Locate the blocks
poster_start = content.find('            <androidx.cardview.widget.CardView\n                android:layout_width="220dp"')
poster_end = content.find('            </androidx.cardview.widget.CardView>', poster_start) + len('            </androidx.cardview.widget.CardView>')

tracking_start = content.find('            <androidx.cardview.widget.CardView\n                android:layout_width="match_parent"', poster_end)
tracking_end = content.find('            </androidx.cardview.widget.CardView>', tracking_start) + len('            </androidx.cardview.widget.CardView>')

title_start = content.find('            <LinearLayout\n                android:layout_width="match_parent"\n                android:layout_height="wrap_content"\n                android:layout_marginStart="20dp"', tracking_end)
title_end = content.find('            </LinearLayout>', title_start) + len('            </LinearLayout>')

details_start = content.find('            <androidx.cardview.widget.CardView\n                android:layout_width="match_parent"', title_end)
# details end is not needed, we just take the rest of the string from details_start

if poster_start != -1 and tracking_start != -1 and title_start != -1 and details_start != -1:
    part1 = content[:tracking_start] # everything up to tracking
    tracking_block = content[tracking_start:tracking_end]
    between_tracking_and_title = content[tracking_end:title_start]
    title_block = content[title_start:title_end]
    between_title_and_details = content[title_end:details_start]
    rest = content[details_start:]
    
    # We want: title_block -> tracking_block
    new_content = part1 + title_block + "\n\n" + tracking_block + "\n\n" + rest
    
    with open('app/src/main/res/layout/activity_detail.xml', 'w') as f:
        f.write(new_content)
    print("Reordered successfully")
else:
    print("Could not find all blocks:")
    print("poster_start:", poster_start)
    print("tracking_start:", tracking_start)
    print("title_start:", title_start)
    print("details_start:", details_start)

