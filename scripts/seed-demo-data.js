const apiUrl = process.env.API_URL || "http://localhost:8081/api/v1";
const token = process.env.TOKEN || ""; // User needs to provide their auth token

async function api(path, method = "GET", body = null) {
  const headers = {
    "Content-Type": "application/json",
    ...(token ? { "Authorization": `Bearer ${token}` } : {})
  };
  const res = await fetch(`${apiUrl}${path}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : null
  });
  if (!res.ok) {
    const err = await res.text();
    console.error(`Error on ${method} ${path}:`, err);
    throw new Error(err);
  }
  return res.json();
}

async function seed() {
  console.log("Seeding Demo Data...");
  
  if (!token) {
    console.error("Please provide a TOKEN environment variable to authenticate.");
    process.exit(1);
  }

  // 1. Create or get Organization
  console.log("Fetching existing Organizations...");
  const orgs = await api("/organizations", "GET");
  let org;
  if (orgs.length > 0) {
    org = orgs[0];
    console.log("Using existing Organization ID:", org.id);
  } else {
    console.log("Creating Organization...");
    org = await api("/organizations", "POST", { name: "TejaInfo", slug: "tejainfo" });
    console.log("Organization ID:", org.id);
  }

  // 1.5. Create or get Workspace
  console.log("Fetching existing Workspaces...");
  const workspaces = await api(`/workspaces?organizationId=${org.id}`, "GET");
  let workspace;
  if (workspaces.length > 0) {
    workspace = workspaces[0];
    console.log("Using existing Workspace ID:", workspace.id);
  } else {
    console.log("Creating Workspace...");
    workspace = await api("/workspaces", "POST", { organizationId: org.id, name: "Internal", slug: "internal" });
    console.log("Workspace ID:", workspace.id);
  }

  // 2. Create or get Projects
  const projectNames = ["Platform", "Infrastructure", "AI Assistant", "Backlog"];
  const projects = [];
  
  console.log("Fetching existing Projects...");
  const existingProjects = await api(`/projects?workspaceId=${workspace.id}`, "GET");
  
  for (const pName of projectNames) {
    const slug = pName.toLowerCase().replace(/ /g, "-");
    let p = existingProjects.find(ep => ep.slug === slug);
    if (p) {
      console.log(`Using existing Project: ${pName}...`);
    } else {
      console.log(`Creating Project: ${pName}...`);
      p = await api("/projects", "POST", { 
        workspaceId: workspace.id, 
        name: pName, 
        slug, 
        description: `Demo project for ${pName}`,
        visibility: "PRIVATE"
      });
    }
    projects.push(p);
  }

  // 3. Create 3 Sprints and Boards for Platform
  const platform = projects[0];
  console.log("Creating Boards and Sprints for Platform...");

  const board1 = await api(`/boards`, "POST", { projectId: platform.id, name: "Platform Dev Board", template: "SCRUM" });
  const board2 = await api(`/boards`, "POST", { projectId: platform.id, name: "Bugs & Triaging", template: "KANBAN" });

  const sprints = [];
  for (let i = 1; i <= 3; i++) {
    const s = await api(`/sprints`, "POST", {
      projectId: platform.id,
      name: `Sprint ${i}`,
      goal: `Complete core features for sprint ${i}`,
      startDate: new Date(Date.now() + i * 14 * 24 * 60 * 60 * 1000).toISOString().split("T")[0],
      endDate: new Date(Date.now() + (i * 14 + 14) * 24 * 60 * 60 * 1000).toISOString().split("T")[0]
    });
    sprints.push(s);
  }

  // 4. Create 100+ tasks
  console.log("Creating 100+ tasks across projects...");
  const statuses = ["TODO", "IN_PROGRESS", "DONE", "BLOCKED"];
  const priorities = ["LOW", "MEDIUM", "HIGH", "URGENT"];
  let taskCount = 0;

  for (let p of projects) {
    for (let i = 0; i < 30; i++) {
      taskCount++;
      const status = statuses[Math.floor(Math.random() * statuses.length)];
      const priority = priorities[Math.floor(Math.random() * priorities.length)];
      const t = await api("/tasks", "POST", {
        projectId: p.id,
        title: `${p.name} Task ${i + 1} - System Update`,
        description: `This is an auto-generated demo task for the ${p.name} project.`,
        priority,
        type: "STORY"
      });
      if (status !== "TODO") {
        await api(`/tasks/${t.id}/status`, "PATCH", { status });
      }
      // Delay to avoid rate limiting
      await new Promise(r => setTimeout(r, 200));
    }
  }

  console.log(`Successfully created ${taskCount} tasks.`);
  console.log("Seeding complete!");
}

seed().catch(console.error);
