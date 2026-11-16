const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_123";
const BOB_UID = "bob_uid_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validProgressPayload(uid) {
  const past = new Date(Date.now() - 2000);
  return {
    userId: uid,
    completedExercises: ["hidden_yawn"],
    totalExerciseRuns: 1,
    completedPrograms: 0,
    egorkiBestCount: 12,
    streakDays: 1,
    courseDayNumber: 1,
    completedDates: ["2026-11-15"],
    lastCompletedCourseDate: "2026-11-15",
    lastAdvancedDate: "",
    createdAt: past,
    updatedAt: past,
  };
}

function validScriptPayload(uid) {
  const past = new Date(Date.now() - 2000);
  return {
    userId: uid,
    genre: "custom",
    genreLabel: "Мой сценарий",
    title: "Проба эфира",
    text: "Центральный *банк* | объявил ↘.",
    createdAt: past,
    updatedAt: past,
  };
}

function validTwisterPayload(uid) {
  const past = new Date(Date.now() - 2000);
  return {
    userId: uid,
    soundTag: "Р / Л",
    difficulty: "Сложная",
    text: "Карл у Клары украл кораллы.",
    createdAt: past,
    updatedAt: past,
  };
}

function validDictionTablePayload(uid) {
  const past = new Date(Date.now() - 2000);
  return {
    userId: uid,
    title: "Взрывные КПТ",
    syllables: ["КПТА", "КПТО", "КПТУ", "КПТЭ", "КПТИ", "КПТЫ"],
    createdAt: past,
    updatedAt: past,
  };
}

function validEvalPayload(uid) {
  const past = new Date(Date.now() - 2000);
  return {
    userId: uid,
    dateLabel: "15.11 15:30",
    scriptTitle: "Экономический обзор",
    modeLabel: "Gemini AI Режиссер",
    headline: "Уверенная подача",
    totalScore: 8,
    chestScore: 8,
    supportScore: 8,
    pauseScore: 9,
    dictionScore: 8,
    critique: "Чуть тише концовки фраз.",
    advice: "Добирайте воздух ребрами перед точкой.",
    recommendedExId: "tarzan_chest",
    createdAt: past,
    updatedAt: past,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write users progress or subcollections", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).set(validProgressPayload(ALICE_UID)));
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("customScripts").get());
});

test("Authenticated user can create, read, and update own progress", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const ref = aliceDb.collection("users").doc(ALICE_UID);
  const initial = validProgressPayload(ALICE_UID);

  await assertSucceeds(ref.set(initial));
  await assertSucceeds(ref.get());
  await assertSucceeds(ref.update({
    totalExerciseRuns: 2,
    egorkiBestCount: 15,
    updatedAt: new Date(Date.now() - 500),
  }));
});

test("Cross-user access is denied on progress and subcollections", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const adminDb = ctx.firestore();
    await adminDb.collection("users").doc(ALICE_UID).set(validProgressPayload(ALICE_UID));
    await adminDb.collection("users").doc(ALICE_UID).collection("customScripts").doc("sc1").set(validScriptPayload(ALICE_UID));
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("customScripts").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("customScripts").doc("sc1").delete());
});

test("Shadow update with ghost field is rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const badPayload = { ...validProgressPayload(ALICE_UID), isAdmin: true };
  await assertFails(aliceDb.collection("users").doc(ALICE_UID).set(badPayload));
});

test("Custom scripts, twisters, diction tables, and AI evaluations CRUD works for owner", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const userDoc = aliceDb.collection("users").doc(ALICE_UID);

  await assertSucceeds(userDoc.collection("customScripts").doc("sc_1").set(validScriptPayload(ALICE_UID)));
  await assertSucceeds(userDoc.collection("customScripts").get());
  await assertSucceeds(userDoc.collection("customScripts").doc("sc_1").delete());

  await assertSucceeds(userDoc.collection("customTwisters").doc("tw_1").set(validTwisterPayload(ALICE_UID)));
  await assertSucceeds(userDoc.collection("customTwisters").get());

  await assertSucceeds(userDoc.collection("customDictionTables").doc("dt_1").set(validDictionTablePayload(ALICE_UID)));
  await assertSucceeds(userDoc.collection("customDictionTables").get());

  await assertSucceeds(userDoc.collection("aiEvaluations").doc("ev_1").set(validEvalPayload(ALICE_UID)));
  await assertSucceeds(userDoc.collection("aiEvaluations").get());
  // Updates on append-only aiEvaluations are blocked
  await assertFails(userDoc.collection("aiEvaluations").doc("ev_1").update({ totalScore: 10 }));
});
