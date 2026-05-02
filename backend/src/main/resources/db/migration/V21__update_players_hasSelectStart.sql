UPDATE players
SET has_selected_starter = TRUE
WHERE selected_digitama IS NOT NULL;