import React from 'react';
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, Legend } from 'recharts';
import { Box, Typography, useTheme } from '@mui/material';
import { motion } from 'framer-motion';

const COLORS = {
    LOW: '#4caf50',
    MEDIUM: '#ff9800',
    HIGH: '#f44336',
    UNKNOWN: '#9e9e9e',
};

const RiskChart = ({ riskLevel }) => {
    const theme = useTheme();

    // Prepare data: Highlight the current risk level
    const data = [
        { name: 'Low', value: riskLevel === 'LOW' ? 100 : 0, color: COLORS.LOW },
        { name: 'Medium', value: riskLevel === 'MEDIUM' ? 100 : 0, color: COLORS.MEDIUM },
        { name: 'High', value: riskLevel === 'HIGH' ? 100 : 0, color: COLORS.HIGH },
    ].filter(item => item.value > 0);

    // If risk level is unknown or not standard
    if (data.length === 0) {
        data.push({ name: 'Unknown', value: 100, color: COLORS.UNKNOWN });
    }

    return (
        <Box
            component={motion.div}
            initial={{ opacity: 0, scale: 0.9 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.5 }}
            sx={{
                width: '100%',
                height: 300,
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                bgcolor: 'background.paper',
                borderRadius: 2,
                p: 2,
                boxShadow: theme.shadows[1]
            }}
        >
            <Typography variant="h6" gutterBottom fontWeight="600">
                Risk Severity
            </Typography>
            <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                    <Pie
                        data={data}
                        cx="50%"
                        cy="50%"
                        innerRadius={60}
                        outerRadius={80}
                        paddingAngle={5}
                        dataKey="value"
                        stroke="none"
                    >
                        {data.map((entry, index) => (
                            <Cell key={`cell-${index}`} fill={entry.color} />
                        ))}
                    </Pie>
                    <Tooltip
                        contentStyle={{ borderRadius: 8, border: 'none', boxShadow: theme.shadows[3] }}
                    />
                    <Legend verticalAlign="bottom" height={36} />
                    <text
                        x="50%"
                        y="50%"
                        textAnchor="middle"
                        dominantBaseline="middle"
                        style={{
                            fontSize: '24px',
                            fontWeight: 'bold',
                            fill: theme.palette.text.primary,
                        }}
                    >
                        {riskLevel || 'N/A'}
                    </text>
                </PieChart>
            </ResponsiveContainer>
        </Box>
    );
};

export default RiskChart;
