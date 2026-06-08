export function GET() {
  return new Response(
    `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 32 32">
      <rect width="32" height="32" rx="6" fill="#2563eb"/>
      <path d="M9 10h14v3H9zm0 6h10v3H9zm0 6h14v3H9z" fill="white"/>
    </svg>`,
    {
      headers: {
        "Content-Type": "image/svg+xml",
        "Cache-Control": "public, max-age=31536000, immutable",
      },
    }
  );
}
