import Container, { type ContainerProps } from "@mui/material/Container";

export type PageContainerProps = ContainerProps;

export function PageContainer({ className = "", ...props }: PageContainerProps) {
  return <Container maxWidth="xl" className={`page-transition ${className}`} {...props} />;
}
