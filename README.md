# taxonomi

Code is in /src/taxonomi/core.clj 

## Thoughts

The word "Taxonomy" seems a weird choice?
Is it not a term for dividing "things" in biology?
Organizational chart fx could be more fitting. 

Most notes are in line with the code since it is meant to be read
from top to bottom following the process while it was implemented. :-)

## Notes

- I used a map for the nodes since it makes the lookup fast and easy.
- There is no handling of automatically setting/updating depths.
- There is no handling of deleting nodes
- There is no handling of moving/changing root

## Routes

- GEH localhost:3000/org
- GET localhost:3000/org/:id/children
- POST localhost:3000/org/:id/move/:new-parent-id
- POST localhost:3000/org/add