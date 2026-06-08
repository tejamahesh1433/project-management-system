import { NextRequest, NextResponse } from "next/server";

const API_BASE_URL =
  process.env.API_BASE_URL ||
  process.env.NEXT_PUBLIC_API_URL ||
  "http://127.0.0.1:8081";

const HOP_BY_HOP_HEADERS = new Set([
  "connection",
  "content-length",
  "keep-alive",
  "proxy-authenticate",
  "proxy-authorization",
  "te",
  "trailer",
  "transfer-encoding",
  "upgrade",
]);

type RouteContext = {
  params: Promise<{
    path: string[];
  }>;
};

function copyHeaders(source: Headers) {
  const headers = new Headers();

  source.forEach((value, key) => {
    if (!HOP_BY_HOP_HEADERS.has(key.toLowerCase())) {
      headers.set(key, value);
    }
  });

  return headers;
}

async function proxyToBackend(request: NextRequest, context: RouteContext) {
  const { path } = await context.params;
  const target = new URL(`/api/${path.join("/")}`, API_BASE_URL);
  target.search = request.nextUrl.search;

  try {
    const response = await fetch(target, {
      method: request.method,
      headers: copyHeaders(request.headers),
      body: ["GET", "HEAD"].includes(request.method)
        ? undefined
        : await request.arrayBuffer(),
      cache: "no-store",
    });

    const headers = copyHeaders(response.headers);

    if (response.status === 204) {
      return new NextResponse(null, { status: 204, headers });
    }

    return new NextResponse(await response.arrayBuffer(), {
      status: response.status,
      headers,
    });
  } catch {
    return NextResponse.json(
      {
        message: "Backend API is unavailable.",
        detail:
          "Start the Spring Boot backend or set API_BASE_URL to the reachable backend URL.",
        backendUrl: API_BASE_URL,
      },
      { status: 503 }
    );
  }
}

export const GET = proxyToBackend;
export const POST = proxyToBackend;
export const PUT = proxyToBackend;
export const PATCH = proxyToBackend;
export const DELETE = proxyToBackend;
export const OPTIONS = proxyToBackend;
