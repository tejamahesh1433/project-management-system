type NotificationListProps = {
  items?: string[];
};

export function NotificationList({ items = [] }: NotificationListProps) {
  return (
    <section>
      {items.map((item) => (
        <article key={item}>{item}</article>
      ))}
    </section>
  );
}
