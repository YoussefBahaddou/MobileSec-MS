import { Card, CardContent, Grid, Typography } from '@mui/material';
import UploadOutlinedIcon from '@mui/icons-material/UploadOutlined';
import InsightsOutlinedIcon from '@mui/icons-material/InsightsOutlined';
import DescriptionOutlinedIcon from '@mui/icons-material/DescriptionOutlined';

const features = [
  {
    title: 'Upload APKs effortlessly',
    description: 'Drag & drop or browse files to submit APKs for automated analysis.',
    icon: <UploadOutlinedIcon fontSize="large" color="primary" />,
  },
  {
    title: 'Inspect risk insights',
    description: 'Review risk scores, permissions, and manifest details per analysis.',
    icon: <InsightsOutlinedIcon fontSize="large" color="primary" />,
  },
  {
    title: 'Generate detailed reports',
    description: 'Download JSON, SARIF, or PDF reports for documentation and sharing.',
    icon: <DescriptionOutlinedIcon fontSize="large" color="primary" />,
  },
];

function DashboardWelcome() {
  return (
    <Grid container spacing={3}>
      <Grid item xs={12}>
        <Typography variant="h4" fontWeight={600} gutterBottom>
          Welcome to MobileSec-MS
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Manage your mobile security analyses with modern tools for uploads, insights, and reporting.
        </Typography>
      </Grid>
      {features.map((feature) => (
        <Grid item xs={12} md={4} key={feature.title}>
          <Card elevation={0} sx={{ height: '100%' }}>
            <CardContent sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
              {feature.icon}
              <Typography variant="h6" fontWeight={600}>
                {feature.title}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                {feature.description}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      ))}
    </Grid>
  );
}

export default DashboardWelcome;
