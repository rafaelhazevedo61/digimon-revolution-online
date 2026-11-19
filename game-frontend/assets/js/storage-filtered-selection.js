storageSelectAllUnlocked = function() {
  storageGetFilteredDigimons()
    .filter(digimon => digimon.locked !== true)
    .forEach(digimon => storageSelectedDigimonIds.add(String(digimon.id)));

  storageRenderList();
};
