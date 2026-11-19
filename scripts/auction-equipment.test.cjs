// Run with: node --test scripts/auction-equipment.test.cjs
const { test } = require("node:test");
const assert = require("node:assert/strict");
const fs = require("node:fs");
const vm = require("node:vm");
const path = require("node:path");

function harness() {
  const elements = new Map();
  function element(id) {
    if (!elements.has(id)) elements.set(id, {
      value: "", innerHTML: "", dataset: {}, label: {}, className: "",
      closest() { return this.label; }, focus() {}, remove() {},
      addEventListener() {}, querySelectorAll() { return []; }
    });
    return elements.get(id);
  }
  const calls = [];
  const errors = [];
  const context = vm.createContext({
    console, encodeURIComponent, decodeURIComponent,
    escapeHtml: value => String(value ?? "").replace(/[&<>"']/g, c => ({
      "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
    })[c]),
    document: { getElementById: element, querySelectorAll: () => [] },
    apiGet: async () => [],
    apiPost: async (url, payload) => { calls.push({ url, payload }); return {}; },
    showToast: (message, type) => { if (type === "error") errors.push(message); }
  });
  vm.runInContext(fs.readFileSync(path.join(__dirname, "../game-frontend/assets/js/auction-house.js"), "utf8"), context);
  context.auctionSetMode = async () => {};
  context.auctionRenderBits = () => {};
  return { context, element, calls, errors, run: code => vm.runInContext(code, context) };
}
const equipment = {
  id: "123e4567-e89b-12d3-a456-426614174000",
  name: "Overclock Blade", rarity: "LEGENDARY", slot: "WEAPON", setCode: "OVERCLOCK",
  tier: 3, refinementLevel: 7, ascensionLevel: 2,
  bonusHp: 20, bonusAttack: 40, bonusDefense: 10,
  effectiveBonusHp: 80, effectiveBonusAttack: 120, effectiveBonusDefense: 50,
  equipped: false, locked: false
};

test("equipment publication preserves UUID, excludes unavailable pieces and fixes quantity to one", async () => {
  const h = harness();
  h.context.apiGet = async url => url === "/equipment/inventory"
    ? [equipment, { ...equipment, id: "locked", locked: true }, { ...equipment, id: "equipped", equipped: true }]
    : {};
  await h.context.auctionShowCreateForm("EQUIPMENT");
  assert.equal(h.run("auctionState.createEligible.length"), 1);
  assert.equal(h.element("auction-create-item").value, equipment.id);
  assert.equal(h.element("auction-create-quantity").label.hidden, true);
  h.element("auction-create-price").value = "1000";
  h.element("auction-create-duration").value = "24";
  h.element("auction-create-quantity").value = "999"; // cannot tamper equipment quantity
  await h.context.auctionCreate({ preventDefault() {} });
  assert.equal(h.calls[0].payload.equipmentId, equipment.id);
  assert.equal(h.calls[0].payload.quantity, 1);
  assert.equal(h.calls[0].payload.listingType, "EQUIPMENT");
  assert.equal(h.calls[0].payload.itemDefinitionId, undefined);
  assert.deepEqual(h.errors, []);
});

test("legacy stackable item publication retains numeric ID and selected quantity", async () => {
  const h = harness();
  h.context.apiGet = async url => url === "/inventory" ? [
    { quantity: 10, itemDefinition: { id: 42, name: "Stone", tradable: true, stackable: true } },
    { quantity: 1, itemDefinition: { id: 43, name: "Bound", tradable: false, stackable: true } }
  ] : {};
  await h.context.auctionShowCreateForm("ITEM");
  // DOM inputs coerce values to strings in the browser.
  h.element("auction-create-item").value = "42";
  h.element("auction-create-quantity").value = "3";
  h.element("auction-create-price").value = "50";
  h.element("auction-create-duration").value = "48";
  await h.context.auctionCreate({ preventDefault() {} });
  assert.equal(h.calls[0].payload.itemDefinitionId, 42);
  assert.equal(h.calls[0].payload.quantity, 3);
  assert.equal(h.calls[0].payload.equipmentId, undefined);
  assert.deepEqual(h.errors, []);
});

test("equipment details carry refinement, ascension and stats, escaping untrusted names", () => {
  const h = harness();
  const details = h.context.auctionEquipmentDetails({ ...equipment, setCode: "<script>alert(1)</script>" });
  assert.match(details, /T3/);
  assert.match(details, /\+7/);
  assert.match(details, /Ascensão 2/);
  assert.match(details, /ATK \+120/);
  assert.ok(!details.includes("<script>"));
  assert.match(details, /&lt;script&gt;/);
  assert.equal(h.context.auctionEquipmentDetails(null), "");
});

test("market card confirmation payload includes immutable equipment details", () => {
  const h = harness();
  const card = h.context.auctionListingCard({
    id: "listing", sellerPlayerId: "seller", sellerUsername: "seller",
    itemName: equipment.name, listingType: "EQUIPMENT", equipment,
    rarity: equipment.rarity, quantity: 1, remainingQuantity: 1, unitPrice: 1000
  });
  assert.match(card, /Ascensão 2/);
  assert.ok(card.includes(encodeURIComponent('"listingType":"EQUIPMENT"')));
  assert.ok(card.includes(encodeURIComponent('"refinementLevel":7')));
});

test("empty inventory cannot publish a stale selected asset", async () => {
  const h = harness();
  await h.context.auctionShowCreateForm("EQUIPMENT");
  h.element("auction-create-item").value = equipment.id;
  await h.context.auctionCreate({ preventDefault() {} });
  assert.equal(h.calls.length, 0);
  assert.equal(h.errors.length, 1);
});

test("equipment selector searches set and rarity without confusing UUID identifiers", async () => {
  const h = harness();
  h.context.apiGet = async () => [equipment];
  await h.context.auctionShowCreateForm("EQUIPMENT");
  h.run('auctionState.createSearch = "overclock"');
  assert.equal(h.context.auctionFilteredCreateItems().length, 1);
  h.run('auctionState.createSearch = "legendary"');
  assert.equal(h.context.auctionFilteredCreateItems().length, 1);
  h.run('auctionState.createSearch = "missing"');
  assert.equal(h.context.auctionFilteredCreateItems().length, 0);
});
