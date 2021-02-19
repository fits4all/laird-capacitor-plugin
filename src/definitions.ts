export interface MainPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
