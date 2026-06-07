type FormMessageProps = {
  type: "error" | "success";
  message: string;
};

export function FormMessage({ type, message }: FormMessageProps) {
  const color = type === "error" ? "text-red-700" : "text-green-700";
  return <p className={`text-sm ${color}`}>{message}</p>;
}
