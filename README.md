# CapstoneProject
Udacity Capstone Project

Please add your Google Maps API Key to the gradle.properties.
Add Google Places API Keys to the strings.xml file.

The basis for a travel companion/planning application.
Currently utilises Location services to get the user's current location, ping the Google Places API to get a list of popular
tourist attractions in that location and display them in a list. The list consists of a provided image, name and it's Google rating.

Home page contains a ViewPager, showing the current location list and saved places.
If a user taps on a place, a detail activity will display an image, the address, a Google Map, and (if available) an extract from Wikipedia.
An FAB allows users to save their favourites places via a Content Provider, into and SQLite database.

