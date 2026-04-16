import re

with open('app/src/main/res/layout/activity_detail.xml', 'r') as f:
    content = f.read()

# The file contains 3 top-level CardViews inside the main LinearLayout.
# We want to swap the second and the third one.
# 1st CardView: Poster (has layout_width="220dp")
# 2nd CardView: Details (has tvRating, tvRuntime)
# 3rd CardView: Tracking (has tvTrackingStatus)

cards = re.findall(r'(\s*<androidx\.cardview\.widget\.CardView[\s\S]*?</androidx\.cardview\.widget\.CardView>)', content)

if len(cards) >= 3:
    # First is poster (we replaced it earlier), it might be inside the cards list.
    # Actually let's just find the text blocks manually.
    pass

# Alternatively, let's use a simpler approach.
# Find the split point between Details CardView and Tracking CardView.
split_str = "</androidx.cardview.widget.CardView>\n\n            <androidx.cardview.widget.CardView\n                android:layout_width=\"match_parent\""

parts = content.split("</androidx.cardview.widget.CardView>")
# The file has 3 CardViews. 
# parts[0] + "</androidx.cardview.widget.CardView>" is the first (poster)
# parts[1] + "</androidx.cardview.widget.CardView>" is the second (details)
# parts[2] + "</androidx.cardview.widget.CardView>" is the third (tracking)
# parts[3] is the rest of the file

if len(parts) >= 4:
    new_content = parts[0] + "</androidx.cardview.widget.CardView>" + parts[2] + "</androidx.cardview.widget.CardView>" + parts[1] + "</androidx.cardview.widget.CardView>" + "".join(parts[3:])
    
    with open('app/src/main/res/layout/activity_detail.xml', 'w') as f:
        f.write(new_content)
    print("Swapped successfully")
else:
    print("Could not find the expected number of CardViews")

